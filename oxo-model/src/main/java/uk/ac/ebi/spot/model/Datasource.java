package uk.ac.ebi.spot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.springframework.beans.factory.annotation.Required;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Simon Jupp
 * @date 11/05/2016
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@NodeEntity (label = "Datasource")
public class Datasource implements Serializable {

    @GraphId
    @JsonIgnore
    private Long id;

    @Property(name="prefix")
    private String prefix;

    @Property(name="idorgNamespace")
    private String idorgNamespace;

    @Property(name="alternatePrefix")
    private Set<String> alternatePrefix = new HashSet<>();

    @Property(name="alternateIris")
    private Set<String> alternateIris = new HashSet<>();

    @Property(name="name")
    private String name;

    @Property(name="orcid")
    private String orcid;

    @Property(name="description")
    private String description;

    @Property(name = "sourceType")
    private SourceType source;

    public Datasource(String prefix, String idorgNamespace, Set<String> alternatePrefix, String name, String description, SourceType source) {
        this.prefix = prefix;
        this.idorgNamespace = idorgNamespace;
        this.alternatePrefix = alternatePrefix;
        this.name = name;
        this.description = description;
        this.source = source;
    }

    public Datasource() {

    }

    public Set<String> getAlternateIris() {
        return alternateIris;
    }

    public void setAlternateIris(Set<String> alternateIris) {
        this.alternateIris = alternateIris;
    }

    public String getIdorgNamespace() {
        return idorgNamespace;
    }

    public void setIdorgNamespace(String idorgNamespace) {
        this.idorgNamespace = idorgNamespace;
    }

    public Set<String> getAlternatePrefix() {
        return alternatePrefix;
    }

    public void setAlternatePrefix(Set<String> alternatePrefix) {
        this.alternatePrefix = alternatePrefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getOrcid() {
        return orcid;
    }

    public void setOrcid(String orcid) {
        this.orcid = orcid;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SourceType getSource() {
        return source;
    }

    public void setSource(SourceType source) {
        this.source = source;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}