package com.sequenceiq.cloudbreak.cm;

import static com.sequenceiq.cloudbreak.polling.PollingResult.isExited;
import static com.sequenceiq.cloudbreak.polling.PollingResult.isTimeout;

import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cloudera.api.swagger.ClustersResourceApi;
import com.cloudera.api.swagger.client.ApiClient;
import com.cloudera.api.swagger.client.ApiException;
import com.cloudera.api.swagger.model.ApiCdhUpgradeArgs;
import com.cloudera.api.swagger.model.ApiCommand;
import com.cloudera.api.swagger.model.ApiCommandList;
import com.sequenceiq.cloudbreak.cloud.scheduler.CancellationException;
import com.sequenceiq.cloudbreak.cm.polling.ClouderaManagerPollingServiceProvider;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.polling.PollingResult;
import com.sequenceiq.cloudbreak.service.CloudbreakException;

@Service
class ClouderaManagerUpgradeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClouderaManagerUpgradeService.class);

    private static final String SUMMARY = "SUMMARY";

    @Inject
    private ClouderaManagerPollingServiceProvider clouderaManagerPollingServiceProvider;

    void callUpgradeCdhCommand(String stackProductVersion, ClustersResourceApi clustersResourceApi, Stack stack, ApiClient apiClient)
            throws ApiException, CloudbreakException {
        LOGGER.debug("Upgrading the CDP Runtime...");
        Optional<ApiCommand> optionalUpgradeCommand = findUpgradeApiCommand(clustersResourceApi, stack);
        try {
            ApiCommand upgradeCommand;
            if (optionalUpgradeCommand.isPresent()) {
                upgradeCommand = optionalUpgradeCommand.get();
                LOGGER.debug("Upgrade of CDP Runtime is already running with id: [{}]", upgradeCommand.getId());
            } else {
                ApiCdhUpgradeArgs upgradeArgs = new ApiCdhUpgradeArgs();
                upgradeArgs.setCdhParcelVersion(stackProductVersion);
                upgradeCommand = clustersResourceApi.upgradeCdhCommand(stack.getName(), upgradeArgs);
            }
            PollingResult pollingResult = clouderaManagerPollingServiceProvider.startPollingCdpRuntimeUpgrade(stack, apiClient, upgradeCommand.getId());
            if (isExited(pollingResult)) {
                throw new CancellationException("Cluster was terminated while waiting for CDP Runtime to be upgraded");
            } else if (isTimeout(pollingResult)) {
                throw new CloudbreakException("Timeout during CDP Runtime upgrade.");
            }
        } catch (ApiException ex) {
            if (ex.getResponseBody().contains("Cannot upgrade because the version is already CDH")) {
                LOGGER.info("The Runtime has already been upgraded to {}", stackProductVersion);
            } else {
                throw ex;
            }
        }
        LOGGER.info("Runtime is successfully upgraded!");
    }

    private Optional<ApiCommand> findUpgradeApiCommand(ClustersResourceApi clustersResourceApi, Stack stack) throws ApiException {
        ApiCommandList apiCommandList = clustersResourceApi.listActiveCommands(stack.getName(), SUMMARY);
        return apiCommandList.getItems().stream()
                .filter(cmd -> "UpgradeCluster".equals(cmd.getName()))
                .findFirst();
    }
}
