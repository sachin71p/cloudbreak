package com.sequenceiq.cloudbreak.cm;

import static com.sequenceiq.cloudbreak.polling.PollingResult.isExited;
import static com.sequenceiq.cloudbreak.polling.PollingResult.isTimeout;

import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cloudera.api.swagger.ClouderaManagerResourceApi;
import com.cloudera.api.swagger.ParcelResourceApi;
import com.cloudera.api.swagger.client.ApiClient;
import com.cloudera.api.swagger.client.ApiException;
import com.cloudera.api.swagger.model.ApiCommand;
import com.cloudera.api.swagger.model.ApiConfig;
import com.cloudera.api.swagger.model.ApiConfigList;
import com.sequenceiq.cloudbreak.cloud.model.ClouderaManagerProduct;
import com.sequenceiq.cloudbreak.cloud.scheduler.CancellationException;
import com.sequenceiq.cloudbreak.cm.model.ParcelResource;
import com.sequenceiq.cloudbreak.cm.polling.ClouderaManagerPollingServiceProvider;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.polling.PollingResult;
import com.sequenceiq.cloudbreak.service.CloudbreakException;

@Service
class ClouderaManagerParcelManagementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClouderaManagerParcelManagementService.class);

    @Inject
    private ClouderaManagerPollingServiceProvider clouderaManagerPollingServiceProvider;

    void setParcelRepos(Set<ClouderaManagerProduct> products, ClouderaManagerResourceApi clouderaManagerResourceApi) throws ApiException {
        Set<String> stackProductParcels = products.stream()
                .map(ClouderaManagerProduct::getParcel)
                .collect(Collectors.toSet());
        LOGGER.info("Setting parcel repo to {}", stackProductParcels);
        ApiConfigList apiConfigList = new ApiConfigList()
                .addItemsItem(new ApiConfig()
                        .name("remote_parcel_repo_urls")
                        .value(String.join(",", stackProductParcels)));
        clouderaManagerResourceApi.updateConfig("Updated configurations.", apiConfigList);
    }

    void refreshParcelRepos(ClouderaManagerResourceApi clouderaManagerResourceApi, Stack stack, ApiClient apiClient) {
        try {
            ApiCommand apiCommand = clouderaManagerResourceApi.refreshParcelRepos();
            clouderaManagerPollingServiceProvider.startPollingCmParcelRepositoryRefresh(stack, apiClient, apiCommand.getId());
        } catch (ApiException e) {
            LOGGER.info("Unable to refresh parcel repo", e);
            throw new ClouderaManagerOperationFailedException(e.getMessage(), e);
        }
    }

    void downloadParcels(Set<ClouderaManagerProduct> products, ParcelResourceApi parcelResourceApi, Stack stack, ApiClient apiClient)
            throws ApiException, CloudbreakException {
        for (ClouderaManagerProduct product : products) {
            LOGGER.info("Downloading {} parcel.", product.getName());
            ApiCommand apiCommand = parcelResourceApi.startDownloadCommand(stack.getName(), product.getName(), product.getVersion());
            PollingResult pollingResult = clouderaManagerPollingServiceProvider.startPollingCdpRuntimeParcelDownload(
                    stack, apiClient, apiCommand.getId(), new ParcelResource(stack.getName(), product.getName(), product.getVersion()));
            if (isExited(pollingResult)) {
                throw new CancellationException("Cluster was terminated while waiting for CDP Runtime Parcel to be downloaded");
            } else if (isTimeout(pollingResult)) {
                throw new CloudbreakException("Timeout during the updated CDP Runtime Parcel download.");
            }
        }
    }

    void distributeParcels(Set<ClouderaManagerProduct> products, ParcelResourceApi parcelResourceApi, Stack stack, ApiClient apiClient)
            throws ApiException, CloudbreakException {
        for (ClouderaManagerProduct product : products) {
            LOGGER.info("Distributing downloaded {} parcel", product.getName());
            ApiCommand apiCommand = parcelResourceApi.startDistributionCommand(stack.getName(), product.getName(), product.getVersion());
            PollingResult pollingResult = clouderaManagerPollingServiceProvider.startPollingCdpRuntimeParcelDistribute(
                    stack, apiClient, apiCommand.getId(), new ParcelResource(stack.getName(), product.getName(), product.getVersion()));
            if (isExited(pollingResult)) {
                throw new CancellationException("Cluster was terminated while waiting for CDP Runtime Parcel to be distributed");
            } else if (isTimeout(pollingResult)) {
                throw new CloudbreakException("Timeout during the updated CDP Runtime Parcel distribution.");
            }
        }
    }

    void activateParcels(Set<ClouderaManagerProduct> products, ParcelResourceApi parcelResourceApi, Stack stack, ApiClient apiClient)
            throws ApiException, CloudbreakException {
        for (ClouderaManagerProduct product : products) {
            String productName = product.getName();
            LOGGER.info("Activating {} parcel", productName);
            ApiCommand apiCommand = parcelResourceApi.activateCommand(stack.getName(), productName, product.getVersion());
            PollingResult result = clouderaManagerPollingServiceProvider.startPollingCmSingleParcelActivation(stack, apiClient, apiCommand.getId(), product);
            if (isExited(result)) {
                throw new CancellationException("Cluster was terminated while waiting for CDP Runtime Parcel to be activated");
            } else if (isTimeout(result)) {
                throw new CloudbreakException("Timeout during the updated CDP Runtime Parcel activation.");
            }
        }
    }

    void checkParcelApiAvailability(Stack stack, ApiClient apiClient) throws CloudbreakException {
        LOGGER.debug("Checking if Parcels API is available");
        PollingResult pollingResult = clouderaManagerPollingServiceProvider.startPollingParcelsApiAvailable(stack, apiClient);
        if (isExited(pollingResult)) {
            throw new CancellationException("Cluster was terminated while waiting for Parcels API to be available");
        } else if (isTimeout(pollingResult)) {
            throw new CloudbreakException("Timeout during waiting for CM Parcels API to be available.");
        }
    }
}
