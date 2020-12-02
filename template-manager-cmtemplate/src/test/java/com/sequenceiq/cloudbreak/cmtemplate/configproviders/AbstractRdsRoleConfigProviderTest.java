package com.sequenceiq.cloudbreak.cmtemplate.configproviders;

import static com.sequenceiq.cloudbreak.TestUtil.rdsConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cloudera.api.swagger.model.ApiClusterTemplateConfig;
import com.sequenceiq.cloudbreak.api.endpoint.v4.database.base.DatabaseType;
import com.sequenceiq.cloudbreak.cmtemplate.CmTemplateProcessor;
import com.sequenceiq.cloudbreak.domain.RDSConfig;
import com.sequenceiq.cloudbreak.template.TemplatePreparationObject;
import com.sequenceiq.cloudbreak.template.views.RdsView;

@ExtendWith(MockitoExtension.class)
class AbstractRdsRoleConfigProviderTest {

    private static final String SSL_CERTS_FILE_PATH = "/foo/bar.pem";

    private AbstractRdsRoleConfigProvider subject;

    @Mock
    private CmTemplateProcessor templateProcessor;

    @Mock
    private TemplatePreparationObject source;

    @BeforeEach
    void setup() {
        subject = new AbstractRdsRoleConfigProvider() {
            @Override
            protected DatabaseType dbType() {
                return DatabaseType.RANGER;
            }

            @Override
            protected List<ApiClusterTemplateConfig> getRoleConfigs(String roleType, TemplatePreparationObject source) {
                return List.of();
            }

            @Override
            public String getServiceType() {
                return "service";
            }

            @Override
            public List<String> getRoleTypes() {
                return List.of("role1", "role2");
            }
        };
    }

    @Test
    void configurationNeededIfRdsConfigAndRoleBothPresent() {
        RDSConfig rdsConfig = rdsConfig(DatabaseType.RANGER);
        when(source.getRdsConfig(DatabaseType.RANGER)).thenReturn(rdsConfig);
        when(templateProcessor.isRoleTypePresentInService(subject.getServiceType(), subject.getRoleTypes())).thenReturn(Boolean.TRUE);

        assertThat(subject.isConfigurationNeeded(templateProcessor, source)).isTrue();
    }

    @Test
    void configurationNotNeededIfRoleAbsent() {
        RDSConfig rdsConfig = rdsConfig(DatabaseType.RANGER);
        when(source.getRdsConfig(DatabaseType.RANGER)).thenReturn(rdsConfig);
        when(templateProcessor.isRoleTypePresentInService(subject.getServiceType(), subject.getRoleTypes())).thenReturn(Boolean.FALSE);

        assertThat(subject.isConfigurationNeeded(templateProcessor, source)).isFalse();
    }

    @Test
    void configurationNotNeededIfRdsConfigAbsent() {
        when(source.getRdsConfig(DatabaseType.RANGER)).thenReturn(null);

        assertThat(subject.isConfigurationNeeded(templateProcessor, source)).isFalse();
    }

    @Test
    void getRdsConfigTestWhenRdsConfigPresent() {
        RDSConfig rdsConfig = rdsConfig(DatabaseType.RANGER);
        when(source.getRdsConfig(DatabaseType.RANGER)).thenReturn(rdsConfig);

        assertThat(subject.getRdsConfig(source)).isSameAs(rdsConfig);
    }

    @Test
    void getRdsConfigTestWhenRdsConfigAbsent() {
        when(source.getRdsConfig(DatabaseType.RANGER)).thenReturn(null);

        assertThat(subject.getRdsConfig(source)).isNull();
    }

    @Test
    void getRdsViewTest() {
        RDSConfig rdsConfig = rdsConfig(DatabaseType.RANGER);
        when(source.getRdsConfig(DatabaseType.RANGER)).thenReturn(rdsConfig);
        when(source.getRdsSslCertificateFile()).thenReturn(SSL_CERTS_FILE_PATH);

        RdsView rdsView = subject.getRdsView(source);

        assertThat(rdsView).isNotNull();
        assertThat(rdsView.getSslCertificateFile()).isEqualTo(SSL_CERTS_FILE_PATH);
    }

}
