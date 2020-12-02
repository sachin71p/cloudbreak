package com.sequenceiq.cloudbreak.structuredevent.rest.filter;

import static com.sequenceiq.cloudbreak.structuredevent.rest.urlparser.CDPRestUrlParser.RESOURCE_CRN;
import static com.sequenceiq.cloudbreak.structuredevent.rest.urlparser.CDPRestUrlParser.RESOURCE_ID;
import static com.sequenceiq.cloudbreak.structuredevent.rest.urlparser.CDPRestUrlParser.RESOURCE_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.sequenceiq.cloudbreak.auth.ThreadBasedUserCrnProvider;
import com.sequenceiq.cloudbreak.auth.altus.Crn;
import com.sequenceiq.cloudbreak.auth.altus.CrnResourceDescriptor;
import com.sequenceiq.cloudbreak.structuredevent.repository.AccountAwareResource;
import com.sequenceiq.cloudbreak.structuredevent.repository.AccountAwareResourceRepository;

@ExtendWith(MockitoExtension.class)
public class DataCollectorComponentTest {

    @InjectMocks
    private RepositoryBasedDataCollector underTest;

    private Map<String, AccountAwareResourceRepository<?, ?>> pathRepositoryMap = new HashMap<>();

    private final String userCrn = new Crn.Builder(CrnResourceDescriptor.USER)
            .setResource("res")
            .setAccountId("acc")
            .build()
            .toString();

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(underTest, "pathRepositoryMap", pathRepositoryMap);
    }

    @Test
    public void testFetchDataFromDbIfNeedWhenNameAndCrnAreNull() {
        AccountAwareResourceRepository<?, ?> repo = mock(AccountAwareResourceRepository.class);
        pathRepositoryMap.put("key", repo);
        Map<String, String> params = new HashMap<>();
        params.put(RESOURCE_CRN, null);
        params.put(RESOURCE_NAME, null);
        ThreadBasedUserCrnProvider.doAs(userCrn, () -> underTest.fetchDataFromDbIfNeed(params));
        Assertions.assertNull(params.get(RESOURCE_NAME));
        Assertions.assertNull(params.get(RESOURCE_CRN));
        Assertions.assertNull(params.get(RESOURCE_ID));
    }

    @Test
    public void testFetchDataFromDbIfNeedWhenNameNotNullAndCrnIsNullButNotFound() {
        AccountAwareResourceRepository<?, ?> repo = mock(AccountAwareResourceRepository.class);
        pathRepositoryMap.put("key", repo);
        Map<String, String> params = new HashMap<>();
        params.put(RESOURCE_CRN, null);
        params.put(RESOURCE_NAME, "name");
        when(repo.findByNameAndAccountId("name", "acc")).thenReturn(Optional.empty());
        ThreadBasedUserCrnProvider.doAs(userCrn, () -> underTest.fetchDataFromDbIfNeed(params));
        Assertions.assertEquals("name", params.get(RESOURCE_NAME));
        Assertions.assertNull(params.get(RESOURCE_CRN));
        Assertions.assertNull(params.get(RESOURCE_ID));

        verify(repo).findByNameAndAccountId("name", "acc");
    }

    @Test
    public void testFetchDataFromDbIfNeedWhenNameNotNullAndCrnIsNullAndFound() {
        AccountAwareResourceRepository<AccountAwareResource, Long> repo = mock(AccountAwareResourceRepository.class);
        AccountAwareResource resource = mock(AccountAwareResource.class);
        pathRepositoryMap.put("key", repo);
        Map<String, String> params = new HashMap<>();
        params.put(RESOURCE_CRN, null);
        params.put(RESOURCE_NAME, "name");
        Optional<AccountAwareResource> entityOpt = Optional.of(resource);

        when(resource.getResourceCrn()).thenReturn("crn-ret");
        when(resource.getId()).thenReturn(342L);
        when(repo.findByNameAndAccountId("name", "acc")).thenReturn(entityOpt);

        ThreadBasedUserCrnProvider.doAs(userCrn, () -> underTest.fetchDataFromDbIfNeed(params));
        Assertions.assertEquals("name", params.get(RESOURCE_NAME));
        Assertions.assertEquals("crn-ret", params.get(RESOURCE_CRN));
        Assertions.assertEquals("342", params.get(RESOURCE_ID));

        verify(repo).findByNameAndAccountId("name", "acc");
    }

    @Test
    public void testFetchDataFromDbIfNeedWhenCrnNotNullAndNameIsNullButNotFound() {
        AccountAwareResourceRepository<?, ?> repo = mock(AccountAwareResourceRepository.class);
        pathRepositoryMap.put("key", repo);
        Map<String, String> params = new HashMap<>();
        params.put(RESOURCE_CRN, "crn");
        params.put(RESOURCE_NAME, null);
        when(repo.findByResourceCrnAndAccountId("crn", "acc")).thenReturn(Optional.empty());
        ThreadBasedUserCrnProvider.doAs(userCrn, () -> underTest.fetchDataFromDbIfNeed(params));
        Assertions.assertNull(params.get(RESOURCE_NAME));
        Assertions.assertEquals("crn", params.get(RESOURCE_CRN));
        Assertions.assertNull(params.get(RESOURCE_ID));

        verify(repo).findByResourceCrnAndAccountId("crn", "acc");
    }

    @Test
    public void testFetchDataFromDbIfNeedWhenCrnNotNullAndNameIsNullAndFound() {
        AccountAwareResourceRepository<AccountAwareResource, Long> repo = mock(AccountAwareResourceRepository.class);
        AccountAwareResource resource = mock(AccountAwareResource.class);
        Optional<AccountAwareResource> entityOpt = Optional.of(resource);
        pathRepositoryMap.put("key", repo);

        Map<String, String> params = new HashMap<>();
        params.put(RESOURCE_CRN, "crn");
        params.put(RESOURCE_NAME, null);

        when(resource.getName()).thenReturn("name-ret");
        when(resource.getId()).thenReturn(342L);
        when(repo.findByResourceCrnAndAccountId("crn", "acc")).thenReturn(entityOpt);
        ThreadBasedUserCrnProvider.doAs(userCrn, () -> underTest.fetchDataFromDbIfNeed(params));

        Assertions.assertEquals("name-ret", params.get(RESOURCE_NAME));
        Assertions.assertEquals("crn", params.get(RESOURCE_CRN));
        Assertions.assertEquals("342", params.get(RESOURCE_ID));

        verify(repo).findByResourceCrnAndAccountId("crn", "acc");
    }
}
