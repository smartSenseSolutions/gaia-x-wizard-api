package eu.gaiax.wizard.core.service.ssl;

import eu.gaiax.wizard.vault.Vault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class CertificateServiceUnitTest {

    @Mock
    private Vault vault;
    /*@Mock
    private DomainService domainService;
    @Mock
    private ParticipantRepository participantRepository;
    @Mock
    private ScheduleService scheduleService;*/
    private CertificateService certificateService;
    private final String randomUUID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
//        this.certificateService = Mockito.spy(new CertificateService(this.vault, this.domainService, this.participantRepository, this.scheduleService));
        this.certificateService = Mockito.spy(new CertificateService(this.vault, null, null, null));
    }

    @AfterEach
    void tearDown() {
        this.certificateService = null;
    }

    @Test
    void testUploadCertificatesToVault() {
        doReturn(new HashMap<String, Object>()).when(this.vault).get(anyString());
        assertDoesNotThrow(() -> this.certificateService.uploadCertificatesToVault(this.randomUUID, this.randomUUID, this.randomUUID, this.randomUUID, this.randomUUID));
    }
}