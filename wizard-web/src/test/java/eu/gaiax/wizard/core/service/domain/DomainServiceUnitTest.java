package eu.gaiax.wizard.core.service.domain;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.ChangeInfo;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import com.amazonaws.services.route53.model.ChangeStatus;
import com.amazonaws.services.route53.model.GetChangeResult;
import eu.gaiax.wizard.api.model.setting.AWSSettings;
import eu.gaiax.wizard.api.utils.StringPool;
import eu.gaiax.wizard.core.service.job.ScheduleService;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import eu.gaiax.wizard.util.constant.TestConstant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.SchedulerException;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DomainServiceUnitTest {


    private DomainService domainService;

    private AWSSettings awsSettings;
    @Mock
    private AmazonRoute53 amazonRoute53;
    @Mock
    private ParticipantRepository participantRepository;
    @Mock
    private ScheduleService scheduleService;

    private final String randomUUID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        this.awsSettings = new AWSSettings(null, null, null, null, this.randomUUID, "0.0.0.0", null, null);
        this.domainService = spy(new DomainService(this.awsSettings, this.amazonRoute53, this.participantRepository, this.scheduleService));
    }

    @AfterEach
    void tearDown() {
        this.domainService = null;
        this.awsSettings = null;
    }

    @Test
    void testUpdateTxtRecordForSSLCertificate_delete() {
        ChangeResourceRecordSetsResult changeResourceRecordSetsResult = new ChangeResourceRecordSetsResult();
        changeResourceRecordSetsResult.setChangeInfo(new ChangeInfo(this.randomUUID, ChangeStatus.INSYNC, new Date()));
        doReturn(changeResourceRecordSetsResult).when(this.amazonRoute53).changeResourceRecordSets(any());

        assertDoesNotThrow(() -> this.domainService.updateTxtRecords(this.randomUUID, this.randomUUID, StringPool.DELETE));
    }

    @Test
    void testUpdateTxtRecordForSSLCertificate_create() {
        ChangeResourceRecordSetsResult changeResourceRecordSetsResult = new ChangeResourceRecordSetsResult()
                .withChangeInfo(new ChangeInfo(this.randomUUID, ChangeStatus.PENDING, new Date()));
        doReturn(changeResourceRecordSetsResult).when(this.amazonRoute53).changeResourceRecordSets(any());
        doReturn(new GetChangeResult().withChangeInfo(new ChangeInfo(this.randomUUID, ChangeStatus.INSYNC, new Date()))).when(this.amazonRoute53).getChange(any());

        assertDoesNotThrow(() -> this.domainService.updateTxtRecords(this.randomUUID, this.randomUUID, StringPool.CREATE));
    }

    @Test
    void testCreateSubDomain() throws SchedulerException {
        Participant participant = this.generateMockParticipant();
        doReturn(Optional.of(participant)).when(this.participantRepository).findById(UUID.fromString(this.randomUUID));
        doReturn(participant).when(this.participantRepository).save(any());

        ChangeResourceRecordSetsResult changeResourceRecordSetsResult = new ChangeResourceRecordSetsResult()
                .withChangeInfo(new ChangeInfo(this.randomUUID, ChangeStatus.PENDING, new Date()));
        doReturn(changeResourceRecordSetsResult).when(this.amazonRoute53).changeResourceRecordSets(any());
        doNothing().when(this.scheduleService).createJob(anyString(), anyString(), anyInt());

        assertDoesNotThrow(() -> this.domainService.createSubDomain(UUID.fromString(this.randomUUID)));
    }

    private Participant generateMockParticipant() {
        Participant participant = new Participant();
        participant.setId(UUID.fromString(this.randomUUID));
        participant.setDomain(TestConstant.SHORT_NAME);
        return participant;
    }
}