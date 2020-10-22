package com.sequenceiq.cloudbreak.cloud.azure;

import static com.sequenceiq.common.api.type.ResourceType.AZURE_VIRTUAL_NETWORK_LINK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.resources.Subscription;
import com.sequenceiq.cloudbreak.cloud.azure.client.AzureClient;
import com.sequenceiq.cloudbreak.cloud.azure.resource.AzureResourceIdProviderService;
import com.sequenceiq.cloudbreak.cloud.azure.view.AzureNetworkView;
import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.context.CloudContext;
import com.sequenceiq.cloudbreak.cloud.exception.CloudConnectorException;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;

@RunWith(MockitoJUnitRunner.class)
public class AzureNetworkLinkServiceTest {

    private static final Long STACK_ID = 12L;

    private static final String RESOURCE_GROUP = "resource-group";

    private static final String NETWORK_ID = "networkId";

    private static final String NETWORK_RG = "networkRg";

    private static final String DEPLOYMENT_ID =
            "/subscriptions/subscription-id/resourceGroups/resourcegroup/providers/Microsoft.Resources/deployments/deployment-id";

    private static final String SUBSCRIPTION_ID = "subscription-id";

    @Mock
    private AzureResourceDeploymentHelperService azureResourceDeploymentHelperService;

    @Mock
    private AzureResourceIdProviderService azureResourceIdProviderService;

    @Mock
    private AzureResourcePersistenceHelperService azureResourcePersistenceHelperService;

    @Mock
    private AzurePrivateEndpointServicesProvider azurePrivateEndpointServicesProvider;

    @InjectMocks
    private AzureNetworkLinkService underTest;

    private AuthenticatedContext ac;

    @Mock
    private AzureClient client;

    @Before
    public void setUp() {
        CloudContext cloudContext = new CloudContext(STACK_ID, "", "", "", "");
        CloudCredential cloudCredential = new CloudCredential(STACK_ID.toString(), "");
        ac = new AuthenticatedContext(cloudContext, cloudCredential);

        when(azureResourceIdProviderService.generateDeploymentId(any(), any(), any())).thenReturn(DEPLOYMENT_ID);
        when(azureResourceDeploymentHelperService.getAzureNetwork(any(), any(), any())).thenReturn(mock(Network.class));
        when(client.getCurrentSubscription()).thenReturn(mock(Subscription.class));
        when(client.getCurrentSubscription().subscriptionId()).thenReturn(SUBSCRIPTION_ID);
        when(azurePrivateEndpointServicesProvider.getEnabledPrivateEndpointServices())
                .thenReturn(List.of(AzurePrivateDnsZoneServiceEnum.STORAGE, AzurePrivateDnsZoneServiceEnum.POSTGRES));
    }

    @Test
    public void testCheckOrCreateWhenNetworkLinkExists() {

        when(client.checkIfNetworkLinksDeployed(any(), any(), any())).thenReturn(true);

        underTest.checkOrCreateNetworkLinks(ac, client, getNetworkView(), RESOURCE_GROUP, Collections.emptyMap());

        verify(azureResourcePersistenceHelperService, times(0)).persistCloudResource(any(), any(), any(), any());
        verify(azureResourcePersistenceHelperService, times(0)).updateCloudResource(any(), any(), any(), any(), any());
        verify(azureResourcePersistenceHelperService, times(0)).isRequested(any(), any());
        verify(azureResourcePersistenceHelperService, times(0)).isCreated(any(), any());
        verify(azureResourceDeploymentHelperService, times(0)).pollForCreation(any(), any());

    }

    @Test
    public void testCheckOrCreateWhenNetworkLinkNotExistsButRequested() {

        when(client.checkIfNetworkLinksDeployed(any(), any(), any())).thenReturn(false);
        when(azureResourcePersistenceHelperService.isRequested(DEPLOYMENT_ID, AZURE_VIRTUAL_NETWORK_LINK)).thenReturn(true);

        underTest.checkOrCreateNetworkLinks(ac, client, getNetworkView(), RESOURCE_GROUP, Collections.emptyMap());

        verify(azureResourcePersistenceHelperService, times(0)).persistCloudResource(any(), any(), any(), any());
        verify(azureResourcePersistenceHelperService, times(0)).updateCloudResource(any(), any(), any(), any(), any());
        verify(azureResourcePersistenceHelperService, times(1)).isRequested(any(), any());
        verify(azureResourcePersistenceHelperService, times(0)).isCreated(any(), any());
        verify(azureResourceDeploymentHelperService, times(1)).pollForCreation(any(), any());
    }

    @Test
    public void testCheckOrCreateWhenNetworkLinkNotExistsAndNotRequestedButAlreadyCreatedInDatabase() {

        when(client.checkIfNetworkLinksDeployed(any(), any(), any())).thenReturn(false);
        when(azureResourcePersistenceHelperService.isRequested(DEPLOYMENT_ID, AZURE_VIRTUAL_NETWORK_LINK)).thenReturn(false);
        when(azureResourcePersistenceHelperService.isCreated(DEPLOYMENT_ID, AZURE_VIRTUAL_NETWORK_LINK)).thenReturn(true);

        underTest.checkOrCreateNetworkLinks(ac, client, getNetworkView(), RESOURCE_GROUP, Collections.emptyMap());

        verify(azureResourcePersistenceHelperService, times(0)).persistCloudResource(any(), any(), any(), any());
        verify(azureResourcePersistenceHelperService, times(2)).updateCloudResource(any(), any(), any(), any(), any());
        verify(azureResourcePersistenceHelperService, times(1)).isRequested(any(), any());
        verify(azureResourcePersistenceHelperService, times(1)).isCreated(any(), any());
        verify(azureResourceDeploymentHelperService, times(0)).pollForCreation(any(), any());
    }

    @Test
    public void testCheckOrCreateWhenNetworkLinkNotExistsAndNotRequestedAndNotCreatedInDatabase() {

        when(client.checkIfNetworkLinksDeployed(any(), any(), any())).thenReturn(false);
        when(azureResourcePersistenceHelperService.isRequested(DEPLOYMENT_ID, AZURE_VIRTUAL_NETWORK_LINK)).thenReturn(false);
        when(azureResourcePersistenceHelperService.isCreated(DEPLOYMENT_ID, AZURE_VIRTUAL_NETWORK_LINK)).thenReturn(false);

        underTest.checkOrCreateNetworkLinks(ac, client, getNetworkView(), RESOURCE_GROUP, Collections.emptyMap());

        verify(azureResourcePersistenceHelperService, times(1)).persistCloudResource(any(), any(), any(), any());
        verify(azureResourcePersistenceHelperService, times(1)).updateCloudResource(any(), any(), any(), any(), any());
        verify(azureResourcePersistenceHelperService, times(1)).isRequested(any(), any());
        verify(azureResourcePersistenceHelperService, times(1)).isCreated(any(), any());
        verify(azureResourceDeploymentHelperService, times(0)).pollForCreation(any(), any());
    }

    @Test(expected = CloudConnectorException.class)
    public void testCheckOrCreateWhenNetworkLinkNotExistsAndNotRequestedAndNotCreatedInDatabaseAndError() {

        when(client.checkIfNetworkLinksDeployed(any(), any(), any())).thenReturn(false);
        when(azureResourcePersistenceHelperService.isRequested(DEPLOYMENT_ID, AZURE_VIRTUAL_NETWORK_LINK)).thenReturn(false);
        when(azureResourcePersistenceHelperService.isCreated(DEPLOYMENT_ID, AZURE_VIRTUAL_NETWORK_LINK)).thenReturn(false);
        doThrow(new CloudConnectorException("", null)).when(azureResourceDeploymentHelperService).deployTemplate(any(), any());

        underTest.checkOrCreateNetworkLinks(ac, client, getNetworkView(), RESOURCE_GROUP, Collections.emptyMap());

        verify(azureResourcePersistenceHelperService, times(1)).persistCloudResource(any(), any(), any(), any());
        verify(azureResourcePersistenceHelperService, times(0)).updateCloudResource(any(), any(), any(), any(), any());
        verify(azureResourcePersistenceHelperService, times(1)).isRequested(any(), any());
        verify(azureResourcePersistenceHelperService, times(1)).isCreated(any(), any());
        verify(azureResourceDeploymentHelperService, times(1)).pollForCreation(any(), any());
    }

    private AzureNetworkView getNetworkView() {
        AzureNetworkView networkView = new AzureNetworkView();
        networkView.setExistingNetwork(false);
        networkView.setNetworkId(NETWORK_ID);
        networkView.setResourceGroupName(NETWORK_RG);
        return networkView;
    }
}