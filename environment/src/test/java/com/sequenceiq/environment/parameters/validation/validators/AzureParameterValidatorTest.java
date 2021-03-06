package com.sequenceiq.environment.parameters.validation.validators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sequenceiq.cloudbreak.auth.altus.EntitlementService;
import com.sequenceiq.cloudbreak.cloud.azure.client.AzureClient;
import com.sequenceiq.cloudbreak.cloud.azure.client.AzureClientService;
import com.sequenceiq.cloudbreak.cloud.init.CloudPlatformConnectors;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.common.mappable.CloudPlatform;
import com.sequenceiq.cloudbreak.validation.ValidationResult;
import com.sequenceiq.environment.credential.v1.converter.CredentialToCloudCredentialConverter;
import com.sequenceiq.environment.environment.dto.EnvironmentDto;
import com.sequenceiq.environment.parameters.dao.domain.ResourceGroupCreation;
import com.sequenceiq.environment.parameters.dao.domain.ResourceGroupUsagePattern;
import com.sequenceiq.environment.parameters.dto.AzureParametersDto;
import com.sequenceiq.environment.parameters.dto.AzureResourceGroupDto;
import com.sequenceiq.environment.parameters.dto.ParametersDto;
import com.sequenceiq.environment.parameters.dto.ParametersDto.Builder;
import com.sequenceiq.environment.parameters.validation.validators.parameter.AzureParameterValidator;

public class AzureParameterValidatorTest {

    @Mock
    private CloudPlatformConnectors cloudPlatformConnectors;

    @Mock
    private AzureClientService azureClientService;

    @Mock
    private CredentialToCloudCredentialConverter credentialToCloudCredentialConverter;

    @Mock
    private EntitlementService entitlementService;

    @InjectMocks
    private AzureParameterValidator underTest;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(entitlementService.azureSingleResourceGroupDeploymentEnabled(anyString(), anyString())).thenReturn(true);
    }

    @Test
    public void testWhenNoAzureParametersThenNoError() {
        EnvironmentDto environmentDto = new EnvironmentDtoBuilder().build();

        ValidationResult validationResult = underTest.validate(environmentDto, environmentDto.getParameters(), ValidationResult.builder());

        assertFalse(validationResult.hasError());
    }

    @Test
    public void testWhenNoResourceGroupThenNoError() {
        EnvironmentDto environmentDto = new EnvironmentDtoBuilder()
                .withAzureParameters(AzureParametersDto.builder().build())
                .build();

        ValidationResult validationResult = underTest.validate(environmentDto, environmentDto.getParameters(), ValidationResult.builder());

        assertFalse(validationResult.hasError());
    }

    @Test
    public void testWhenUseMultipleResourceGroupsThenNoError() {
        EnvironmentDto environmentDto = new EnvironmentDtoBuilder()
                .withAzureParameters(AzureParametersDto.builder()
                        .withResourceGroup(AzureResourceGroupDto.builder()
                                .withResourceGroupUsagePattern(ResourceGroupUsagePattern.USE_MULTIPLE).build())
                        .build())
                .build();

        ValidationResult validationResult = underTest.validate(environmentDto, environmentDto.getParameters(), ValidationResult.builder());

        assertFalse(validationResult.hasError());
    }

    @Test
    public void testWhenUseExistingResourceGroupAndExistsThenNoError() {
        EnvironmentDto environmentDto = new EnvironmentDtoBuilder()
                .withAzureParameters(AzureParametersDto.builder()
                        .withResourceGroup(AzureResourceGroupDto.builder()
                                .withResourceGroupUsagePattern(ResourceGroupUsagePattern.USE_SINGLE)
                                .withResourceGroupCreation(ResourceGroupCreation.USE_EXISTING)
                                .withName("myResourceGroup").build())
                        .build())
                .build();
        when(credentialToCloudCredentialConverter.convert(any())).thenReturn(new CloudCredential());
        AzureClient azureClient = mock(AzureClient.class);
        when(azureClientService.getClient(any())).thenReturn(azureClient);
        when(azureClient.resourceGroupExists("myResourceGroup")).thenReturn(true);

        ValidationResult validationResult = underTest.validate(environmentDto, environmentDto.getParameters(), ValidationResult.builder());

        assertFalse(validationResult.hasError());
    }

    @Test
    public void testWhenMultipleResourceGroupAndEmptyNameThenNoError() {
        EnvironmentDto environmentDto = new EnvironmentDtoBuilder()
                .withAzureParameters(AzureParametersDto.builder()
                        .withResourceGroup(AzureResourceGroupDto.builder()
                                .withResourceGroupUsagePattern(ResourceGroupUsagePattern.USE_MULTIPLE)
                                .withResourceGroupCreation(ResourceGroupCreation.USE_EXISTING)
                                .build())
                        .build())
                .build();
        when(credentialToCloudCredentialConverter.convert(any())).thenReturn(new CloudCredential());
        AzureClient azureClient = mock(AzureClient.class);
        when(azureClientService.getClient(any())).thenReturn(azureClient);
        when(azureClient.resourceGroupExists("myResourceGroup")).thenReturn(true);

        ValidationResult validationResult = underTest.validate(environmentDto, environmentDto.getParameters(), ValidationResult.builder());

        assertFalse(validationResult.hasError());
    }

    @Test
    public void testWhenMultipleResourceGroupAndNonEmptyNameThenError() {
        EnvironmentDto environmentDto = new EnvironmentDtoBuilder()
                .withAzureParameters(AzureParametersDto.builder()
                        .withResourceGroup(AzureResourceGroupDto.builder()
                                .withResourceGroupUsagePattern(ResourceGroupUsagePattern.USE_MULTIPLE)
                                .withResourceGroupCreation(ResourceGroupCreation.USE_EXISTING)
                                .withName("myResourceGroup").build())
                        .build())
                .build();
        when(credentialToCloudCredentialConverter.convert(any())).thenReturn(new CloudCredential());
        AzureClient azureClient = mock(AzureClient.class);
        when(azureClientService.getClient(any())).thenReturn(azureClient);
        when(azureClient.resourceGroupExists("myResourceGroup")).thenReturn(false);

        ValidationResult validationResult = underTest.validate(environmentDto, environmentDto.getParameters(), ValidationResult.builder());

        assertTrue(validationResult.hasError());
        assertEquals("1. You specified to use multiple resource groups for your resources, " +
                        "but then the single resource group name 'myResourceGroup' cannot not be specified.",
                validationResult.getFormattedErrors());
    }

    @Test
    public void testWhenUseExistingResourceGroupAndEmptyNameThenError() {
        EnvironmentDto environmentDto = new EnvironmentDtoBuilder()
                .withAzureParameters(AzureParametersDto.builder()
                        .withResourceGroup(AzureResourceGroupDto.builder()
                                .withResourceGroupUsagePattern(ResourceGroupUsagePattern.USE_SINGLE)
                                .withResourceGroupCreation(ResourceGroupCreation.USE_EXISTING)
                                .withName("").build())
                        .build())
                .build();
        when(credentialToCloudCredentialConverter.convert(any())).thenReturn(new CloudCredential());
        AzureClient azureClient = mock(AzureClient.class);
        when(azureClientService.getClient(any())).thenReturn(azureClient);

        ValidationResult validationResult = underTest.validate(environmentDto, environmentDto.getParameters(), ValidationResult.builder());

        assertTrue(validationResult.hasError());
        assertEquals("1. If you use a single resource group for your resources then please provide the name of that resource group.",
                validationResult.getFormattedErrors());
    }

    @Test
    public void testWhenUseExistingResourceGroupAndEmptyResourceGroupUsageThenError() {
        EnvironmentDto environmentDto = new EnvironmentDtoBuilder()
                .withAzureParameters(AzureParametersDto.builder()
                        .withResourceGroup(AzureResourceGroupDto.builder()
                                .withResourceGroupCreation(ResourceGroupCreation.USE_EXISTING)
                                .withName("myResourceGroup").build())
                        .build())
                .build();
        when(credentialToCloudCredentialConverter.convert(any())).thenReturn(new CloudCredential());
        AzureClient azureClient = mock(AzureClient.class);
        when(azureClientService.getClient(any())).thenReturn(azureClient);

        ValidationResult validationResult = underTest.validate(environmentDto, environmentDto.getParameters(), ValidationResult.builder());

        assertTrue(validationResult.hasError());
        assertEquals("1. If you have provided the resource group name for your resources then please provide the resource group usage pattern too.",
                validationResult.getFormattedErrors());
    }

    @Test
    public void testWhenUseExistingResourceGroupAndNotExistsThenError() {
        EnvironmentDto environmentDto = new EnvironmentDtoBuilder()
                .withAzureParameters(AzureParametersDto.builder()
                        .withResourceGroup(AzureResourceGroupDto.builder()
                                .withResourceGroupUsagePattern(ResourceGroupUsagePattern.USE_SINGLE)
                                .withResourceGroupCreation(ResourceGroupCreation.USE_EXISTING)
                                .withName("myResourceGroup").build())
                        .build())
                .build();
        when(credentialToCloudCredentialConverter.convert(any())).thenReturn(new CloudCredential());
        AzureClient azureClient = mock(AzureClient.class);
        when(azureClientService.getClient(any())).thenReturn(azureClient);
        when(azureClient.resourceGroupExists("myResourceGroup")).thenReturn(false);

        ValidationResult validationResult = underTest.validate(environmentDto, environmentDto.getParameters(), ValidationResult.builder());

        assertTrue(validationResult.hasError());
        assertEquals("1. Resource group 'myResourceGroup' does not exist or insufficient permission to access it.", validationResult.getFormattedErrors());
    }

    @Test
    public void testWhenFeatureTurnedOffAndUseMultipleThenNoError() {
        EnvironmentDto environmentDto = new EnvironmentDtoBuilder()
                .withAzureParameters(AzureParametersDto.builder()
                        .withResourceGroup(AzureResourceGroupDto.builder()
                                .withResourceGroupUsagePattern(ResourceGroupUsagePattern.USE_MULTIPLE).build())
                        .build())
                .build();
        when(credentialToCloudCredentialConverter.convert(any())).thenReturn(new CloudCredential());
        AzureClient azureClient = mock(AzureClient.class);
        when(azureClientService.getClient(any())).thenReturn(azureClient);
        when(azureClient.resourceGroupExists("myResourceGroup")).thenReturn(false);
        when(entitlementService.azureSingleResourceGroupDeploymentEnabled(anyString(), anyString())).thenReturn(false);

        ValidationResult validationResult = underTest.validate(environmentDto, environmentDto.getParameters(), ValidationResult.builder());

        assertFalse(validationResult.hasError());
        verify(credentialToCloudCredentialConverter, never()).convert(any());
        verify(azureClientService, never()).getClient(any());
        verify(entitlementService, times(0)).azureSingleResourceGroupDeploymentEnabled(anyString(), anyString());
    }

    @Test
    public void testWhenFeatureTurnedOffAndUseSingleThenError() {
        EnvironmentDto environmentDto = new EnvironmentDtoBuilder()
                .withAzureParameters(AzureParametersDto.builder()
                        .withResourceGroup(AzureResourceGroupDto.builder()
                                .withResourceGroupUsagePattern(ResourceGroupUsagePattern.USE_SINGLE)
                                .withResourceGroupCreation(ResourceGroupCreation.USE_EXISTING)
                                .withName("myResourceGroup").build())
                        .build())
                .build();
        when(credentialToCloudCredentialConverter.convert(any())).thenReturn(new CloudCredential());
        AzureClient azureClient = mock(AzureClient.class);
        when(azureClientService.getClient(any())).thenReturn(azureClient);
        when(azureClient.resourceGroupExists("myResourceGroup")).thenReturn(false);
        when(entitlementService.azureSingleResourceGroupDeploymentEnabled(anyString(), anyString())).thenReturn(false);

        ValidationResult validationResult = underTest.validate(environmentDto, environmentDto.getParameters(), ValidationResult.builder());

        assertTrue(validationResult.hasError());
        assertEquals("1. You specified to use a single resource group for all of your resources, but that feature is currently disabled",
                validationResult.getFormattedErrors());
    }

    @Test
    public void testWhenDedicatedStorageAccountFeatureTurnedOffAndUseSingleThenError() {
        EnvironmentDto environmentDto = new EnvironmentDtoBuilder()
                .withAzureParameters(AzureParametersDto.builder()
                        .withResourceGroup(AzureResourceGroupDto.builder()
                                .withResourceGroupUsagePattern(ResourceGroupUsagePattern.USE_SINGLE_WITH_DEDICATED_STORAGE_ACCOUNT)
                                .withResourceGroupCreation(ResourceGroupCreation.USE_EXISTING)
                                .withName("myResourceGroup").build())
                        .build())
                .build();
        when(credentialToCloudCredentialConverter.convert(any())).thenReturn(new CloudCredential());
        AzureClient azureClient = mock(AzureClient.class);
        when(azureClientService.getClient(any())).thenReturn(azureClient);
        when(azureClient.resourceGroupExists("myResourceGroup")).thenReturn(false);
        when(entitlementService.azureSingleResourceGroupDeploymentEnabled(anyString(), anyString())).thenReturn(true);
        when(entitlementService.azureSingleResourceGroupDedicatedStorageAccountEnabled(anyString(), anyString())).thenReturn(false);

        ValidationResult validationResult = underTest.validate(environmentDto, environmentDto.getParameters(), ValidationResult.builder());

        assertTrue(validationResult.hasError());
        assertEquals("1. You specified to use a single resource group with dedicated storage account for the images, but that feature is currently disabled",
                validationResult.getFormattedErrors());
    }

    @Test
    public void testCloudPlatform() {
        assertEquals(CloudPlatform.AZURE, underTest.getcloudPlatform());
    }

    private static class EnvironmentDtoBuilder {

        private static final String ACCOUNT_ID = "accountId";

        private final EnvironmentDto environmentDto = new EnvironmentDto();

        private final Builder parametersDtoBuilder = ParametersDto.builder();

        public EnvironmentDtoBuilder withAzureParameters(AzureParametersDto azureParametersDto) {
            parametersDtoBuilder.withAzureParameters(azureParametersDto);
            return this;
        }

        public EnvironmentDto build() {
            ParametersDto parametersDto = parametersDtoBuilder.build();
            environmentDto.setParameters(parametersDto);
            environmentDto.setAccountId(ACCOUNT_ID);
            return environmentDto;
        }
    }
}
