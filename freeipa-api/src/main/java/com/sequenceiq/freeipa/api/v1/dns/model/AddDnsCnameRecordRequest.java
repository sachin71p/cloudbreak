package com.sequenceiq.freeipa.api.v1.dns.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sequenceiq.freeipa.api.v1.dns.doc.DnsModelDescription;
import com.sequenceiq.service.api.doc.ModelDescriptions;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("AddDnsCnameRecordV1Request")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddDnsCnameRecordRequest {
    @NotNull
    @ApiModelProperty(value = ModelDescriptions.ENVIRONMENT_CRN, required = true)
    private String environmentCrn;

    @NotEmpty
    @ApiModelProperty(value = DnsModelDescription.CNAME, required = true)
    private String cname;

    @ApiModelProperty(DnsModelDescription.DNS_ZONE)
    private String dnsZone;

    @NotEmpty
    @ApiModelProperty(value = DnsModelDescription.CNAME_TARGET_FQDN, required = true)
    private String targetFqdn;

    public String getEnvironmentCrn() {
        return environmentCrn;
    }

    public void setEnvironmentCrn(String environmentCrn) {
        this.environmentCrn = environmentCrn;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getDnsZone() {
        return dnsZone;
    }

    public void setDnsZone(String dnsZone) {
        this.dnsZone = dnsZone;
    }

    public String getTargetFqdn() {
        return targetFqdn;
    }

    public void setTargetFqdn(String targetFqdn) {
        this.targetFqdn = targetFqdn;
    }
}
