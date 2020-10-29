package com.sequenceiq.cloudbreak.auth.altus.model;

public enum Entitlement {

    DATAHUB_FLOW_SCALING,
    DATAHUB_STREAMING_SCALING,
    DATAHUB_DEFAULT_SCALING,
    CDP_AZURE,
    CDP_GCP,
    CDP_BASE_IMAGE,
    CDP_AUTOMATIC_USERSYNC_POLLER,
    CDP_FREEIPA_HA,
    CDP_FREEIPA_HA_REPAIR,
    CDP_FREEIPA_HEALTH_CHECK,
    CLOUDERA_INTERNAL_ACCOUNT,
    CDP_FMS_CLUSTER_PROXY,
    CDP_CLOUD_STORAGE_VALIDATION,
    CDP_RAZ,
    CDP_MEDIUM_DUTY_SDX,
    CDP_RUNTIME_UPGRADE,
    CDP_FREEIPA_DL_EBS_ENCRYPTION,
    LOCAL_DEV,
    CDP_AZURE_SINGLE_RESOURCE_GROUP,
    CDP_AZURE_SINGLE_RESOURCE_GROUP_DEDICATED_STORAGE_ACCOUNT,
    CDP_CB_FAST_EBS_ENCRYPTION,
    CDP_CLOUD_IDENTITY_MAPPING,
    CDP_ALLOW_INTERNAL_REPOSITORY_FOR_UPGRADE,
    CDP_UMS_USER_SYNC_MODEL_GENERATION,
    CDP_SDX_HBASE_CLOUD_STORAGE,
    CB_AUTHZ_POWER_USERS,
    CDP_ALLOW_DIFFERENT_DATAHUB_VERSION_THAN_DATALAKE,
    DATAHUB_AWS_AUTOSCALING,
    DATAHUB_AZURE_AUTOSCALING,
    CDP_CB_DATABASE_WIRE_ENCRYPTION;
}
