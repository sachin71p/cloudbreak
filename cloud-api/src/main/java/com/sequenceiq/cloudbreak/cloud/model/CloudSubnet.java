package com.sequenceiq.cloudbreak.cloud.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sequenceiq.cloudbreak.cloud.model.generic.DynamicModel;
import com.sequenceiq.cloudbreak.cloud.model.network.SubnetType;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudSubnet extends DynamicModel implements Serializable {

    private String id;

    private String name;

    private String availabilityZone;

    private String cidr;

    private SubnetType type;

    private boolean privateSubnet;

    private boolean mapPublicIpOnLaunch;

    private boolean igwAvailable;

    private Map<String, String> properties = new HashMap<>();

    public CloudSubnet() {
    }

    public CloudSubnet(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public CloudSubnet(String id, String name, String availabilityZone, String cidr) {
        this.id = id;
        this.name = name;
        this.availabilityZone = availabilityZone;
        this.cidr = cidr;
    }

    public CloudSubnet(String id, String name, String availabilityZone, String cidr, boolean privateSubnet, boolean mapPublicIpOnLaunch, boolean igwAvailable,
            SubnetType type) {
        this.id = id;
        this.name = name;
        this.availabilityZone = availabilityZone;
        this.cidr = cidr;
        this.privateSubnet = privateSubnet;
        this.mapPublicIpOnLaunch = mapPublicIpOnLaunch;
        this.igwAvailable = igwAvailable;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    public String getCidr() {
        return cidr;
    }

    public void setCidr(String cidr) {
        this.cidr = cidr;
    }

    public boolean isPrivateSubnet() {
        return privateSubnet;
    }

    public void setPrivateSubnet(boolean privateSubnet) {
        this.privateSubnet = privateSubnet;
    }

    public boolean isMapPublicIpOnLaunch() {
        return mapPublicIpOnLaunch;
    }

    public void setMapPublicIpOnLaunch(boolean mapPublicIpOnLaunch) {
        this.mapPublicIpOnLaunch = mapPublicIpOnLaunch;
    }

    public boolean isIgwAvailable() {
        return igwAvailable;
    }

    public void setIgwAvailable(boolean igwAvailable) {
        this.igwAvailable = igwAvailable;
    }

    public SubnetType getType() {
        return type;
    }

    public void setType(SubnetType type) {
        this.type = type;
    }

    public CloudSubnet withId(String newId) {
        return new CloudSubnet(newId, name, availabilityZone, cidr, privateSubnet, mapPublicIpOnLaunch, igwAvailable, type);
    }

    public void withProperty(String key, String value) {
        properties.put(key, value);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        CloudSubnet that = (CloudSubnet) o;
        return privateSubnet == that.privateSubnet &&
                mapPublicIpOnLaunch == that.mapPublicIpOnLaunch &&
                igwAvailable == that.igwAvailable &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(availabilityZone, that.availabilityZone) &&
                Objects.equals(cidr, that.cidr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, availabilityZone, cidr, privateSubnet, mapPublicIpOnLaunch, igwAvailable);
    }

    @Override
    public String toString() {
        return "CloudSubnet{"
                + "id='" + id + '\''
                + ", name='" + name + '\''
                + ", availabilityZone='" + availabilityZone + '\''
                + ", cidr='" + cidr + '\''
                + ", privateSubnet=" + privateSubnet
                + ", mapPublicIpOnLaunch=" + mapPublicIpOnLaunch
                + ", igwAvailable=" + igwAvailable
                + '}';
    }
}
