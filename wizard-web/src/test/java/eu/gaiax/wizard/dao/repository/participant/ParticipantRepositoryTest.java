package eu.gaiax.wizard.dao.repository.participant;

import eu.gaiax.wizard.dao.entity.participant.Participant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static eu.gaiax.wizard.util.constant.TestConstant.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ParticipantRepositoryTest {

    @Autowired
    private ParticipantRepository participantRepository;

    Participant participant;

    @BeforeEach
    void setUp() {

        this.participant = Participant.builder()
                .email(EMAIL)
                .legalName(LEGAL_NAME)
                .shortName(SHORT_NAME)
                .domain(DOMAIN)
                .did(DID)
                .build();

        this.participantRepository.save(this.participant);
    }

    @AfterEach
    void tearDown() {
        this.participant = null;
        this.participantRepository.deleteAll();
    }

    @Test
    void testGetByEmail_Found() {
        Participant existingParticipant = this.participantRepository.getByEmail(EMAIL);
        assertThat(existingParticipant.getEmail()).isEqualTo(this.participant.getEmail());
    }

    @Test
    void testGetByEmail_NotFound() {
        Participant existingParticipant = this.participantRepository.getByEmail(LEGAL_NAME);
        assertThat(existingParticipant.getEmail()).isNotEqualTo(this.participant.getEmail());
    }

    @Test
    void testGetByDomain_Found() {
        Participant existingParticipant = this.participantRepository.getByDomain(DOMAIN);
        assertThat(existingParticipant.getDomain()).isEqualTo(this.participant.getDomain());
    }

    @Test
    void testGetByDomain_NotFound() {
        Participant existingParticipant = this.participantRepository.getByDomain(LEGAL_NAME);
        assertThat(existingParticipant.getDomain()).isNotEqualTo(this.participant.getDomain());
    }

    @Test
    void testGetByDid_Found() {
        Participant existingParticipant = this.participantRepository.getByDid(DID);
        assertThat(existingParticipant.getDid()).isEqualTo(this.participant.getDid());
    }

    @Test
    void testGetByDid_NotFound() {
        Participant existingParticipant = this.participantRepository.getByDid(LEGAL_NAME);
        assertThat(existingParticipant.getDid()).isNotEqualTo(this.participant.getDid());
    }

    @Test
    void testGetByLegalName_Found() {
        Participant existingParticipant = this.participantRepository.getByLegalName(LEGAL_NAME);
        assertThat(existingParticipant.getLegalName()).isEqualTo(this.participant.getLegalName());
    }

    @Test
    void testGetByLegalName_NotFound() {
        Participant existingParticipant = this.participantRepository.getByLegalName(SHORT_NAME);
        assertThat(existingParticipant.getLegalName()).isNotEqualTo(this.participant.getLegalName());
    }

    @Test
    void testGetByShortName_Found() {
        Participant existingParticipant = this.participantRepository.getByShortName(SHORT_NAME);
        assertThat(existingParticipant.getShortName()).isEqualTo(this.participant.getShortName());
    }

    @Test
    void testGetByShortName_NotFound() {
        Participant existingParticipant = this.participantRepository.getByShortName(LEGAL_NAME);
        assertThat(existingParticipant.getShortName()).isNotEqualTo(this.participant.getShortName());
    }

    @Test
    void testExistsByEmail_Found() {
        boolean participantExists = this.participantRepository.existsByEmail(EMAIL);
        assertThat(participantExists).isTrue();
    }

    @Test
    void testExistsByEmail_NotFound() {
        boolean participantExists = this.participantRepository.existsByEmail(LEGAL_NAME);
        assertThat(participantExists).isFalse();
    }
}
