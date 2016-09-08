package uk.ac.ebi.spot.loader;

import org.semanticweb.owlapi.model.IRI;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.model.*;
import uk.ac.ebi.spot.model.Datasource;
import uk.ac.ebi.spot.model.Mapping;
import uk.ac.ebi.spot.model.MappingSource;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.exception.OntologyLoadingException;
import uk.ac.ebi.spot.ols.loader.OntologyLoader;
import uk.ac.ebi.spot.ols.loader.OntologyLoaderFactory;
import uk.ac.ebi.spot.ols.util.OBOXref;
import uk.ac.ebi.spot.service.MappingBuilder;

import java.net.URI;
import java.net.URL;
import java.util.*;

/**
 * @author Simon Jupp
 * @date 11/05/2016
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component
public class OBOLoader implements Loader {


    public OBOLoader() {


    }

    @Override
    public Collection<MappingSource> load() {


        Collection<MappingSource> mappingSources = new HashSet<>();

        List<OntologyResourceConfig> configs = new ArrayList<>();

        configs.add(new OntologyResourceConfig.OntologyResourceConfigBuilder("http://purl.obolibrary.org/obo/hp", "Human Phenotype Ontology", "hp", URI.create("http://www.ebi.ac.uk/ols/ontologies/hp/download"))
                .setBaseUris(Collections.singleton("http://purl.obolibrary.org/obo/HP_")).build());


        configs.add(new OntologyResourceConfig.OntologyResourceConfigBuilder("http://purl.obolibrary.org/obo/doid", "Human disease Ontology", "doid", URI.create("http://www.ebi.ac.uk/ols/ontologies/doid/download"))
                .setBaseUris(Collections.singleton("http://purl.obolibrary.org/obo/DOID_")).build());


        configs.add(new OntologyResourceConfig.OntologyResourceConfigBuilder("http://www.ebi.ac.uk/efo", "Experimental Factor Ontology", "efo", URI.create("http://www.ebi.ac.uk/ols/ontologies/efo/download"))
                .setBaseUris(Collections.singleton("http://www.ebi.ac.uk/efo/EFO_")).build());


        configs.add(new OntologyResourceConfig.OntologyResourceConfigBuilder("http://www.orpha.net/ontology/orphanet.owl", "Orphanet Rare Disease Ontology", "ordo", URI.create("http://www.ebi.ac.uk/ols/ontologies/ordo/download"))
                .setBaseUris(Collections.singleton("http://www.orpha.net/ORDO/Orphanet_")).build());

//        configs.add(new OntologyResourceConfig.OntologyResourceConfigBuilder("http://purl.obolibrary.org/obo/uberon", "Uber-anatomy ontology", "uberon", URI.create("http://www.ebi.ac.uk/ols/ontologies/uberon/download"))
//                .setBaseUris(Collections.singleton("http://purl.obolibrary.org/obo/UBERON_")).build());


        Set<String> properties = new HashSet<>();
        properties.add("database_cross_reference");
        properties.add("definition_citation");
        properties.add("has_alternative_id");
        properties.add("hasDbXref");


        for (OntologyResourceConfig config : configs) {
            Collection<Mapping> mappings = new HashSet<>();

            Datasource datasource = new Datasource(config.getNamespace(), null, Collections.emptySet(), config.getTitle(), config.getDescription(), SourceType.ONTOLOGY);

            try {
                OntologyLoader loader = OntologyLoaderFactory.getLoader(config);

                Collection<IRI> classIrirs = loader.getAllClasses();
                for (IRI iri: classIrirs) {
                    String fromCurie = loader.getOboId(iri);

                    if (fromCurie == null) {
                       fromCurie = loader.getShortForm(iri);
                    }

                    Datasource localSource = null;
                    if (loader.isLocalTerm(iri)) {
                        localSource = datasource;
                    }

                    String id = fromCurie;

                    if (id.split(":").length == 2) {
                        id = id.split(":")[1];
                    }

                    Term fromTerm = new Term(fromCurie, id, iri.toString(), loader.getTermLabels().get(iri), datasource);


                    Map<IRI, Collection<String>> annotations = loader.getAnnotations(iri);

                    if (!loader.getOBOXrefs(iri).isEmpty()) {
                        for (OBOXref xrefs : loader.getOBOXrefs(iri)) {
                            if (xrefs.getDatabase() != null && xrefs.getId() != null) {
                                String s = xrefs.getDatabase() + ":" + xrefs.getId();
                                s = cleanupHack(s);
//                                IdentifierType type = IdentifierType.PREFIXED;
//                                mappings.add(new MappingBuilder(iri.toString(), IdentifierType.URI, localSource,  s, type, null, MappingType.XREF, datasource ).setScope(Scope.RELATED).setSource(SourceType.ONTOLOGY).build());
//
                            }
                        }
                    }
                    else {
                        for (IRI annotationPropertyIri : annotations.keySet()) {

                            if (loader.getTermLabels().containsKey(annotationPropertyIri)) {
                                String propertyLabel =  loader.getTermLabels().get(annotationPropertyIri);

                                for (String lookupProps : properties) {
                                    if (propertyLabel.contains(lookupProps))   {

                                        for (String s : annotations.get(annotationPropertyIri)) {


                                            s = cleanupHack(s);

                                            try {
                                                new URL(s);
                                            } catch (Exception e) {
                                                if (s.split(":").length == 2) {

                                                }
                                                // its not a URI
                                            }
//                                            mappings.add(new MappingBuilder(iri.toString(), IdentifierType.URI, localSource,  s, type, null, properties.get(lookupProps), datasource ).setScope(Scope.RELATED).setSource(SourceType.ONTOLOGY).build());
                                        }
                                    }

                                }
                            }
                        }
                    }
                }

                mappingSources.add(new MappingSource(datasource, mappings));

            } catch (OntologyLoadingException e) {
                e.printStackTrace();
            }
        }

        return mappingSources;
    }

    /**
     * Method to normalise some known prefix variants - todo move this to external config
     * @param s
     * @return
     */
    public String cleanupHack (String s) {

        if (s.split(":")[0].matches("ICD.*10.*")) {
            s = "IDC10:" + s.split(":")[1];
        }
        if (s.split(":")[0].matches("ICD.*9.*")) {
            s = "IDC9:" + s.split(":")[1];
        }
        if (s.split(":")[0].equals("MSH")) {
            s = "MeSH:" + s.split(":")[1];
        }
        if (s.split(":")[0].startsWith("KEGG")) {
            s = "KEGG:" + s.split(":")[1];
        }
        if (s.split(":")[0].startsWith("nci")) {
            s = "NCIT:" + s.split(":")[1];
        }
        if (s.split(":")[0].startsWith("snomed")) {
            s = "SNOMEDCT:" + s.split(":")[1];
        }
        if (s.split(":")[0].startsWith("umls")) {
            s = "UMLS:" + s.split(":")[1];
        }

        return s;

    }
}