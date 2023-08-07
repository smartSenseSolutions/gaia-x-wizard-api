package eu.gaiax.wizard.core.service.service_offer;

import eu.gaiax.wizard.api.model.service_offer.ResourceRequest;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.entity.resource.Resource;
import eu.gaiax.wizard.dao.entity.service_offer.ServiceOffer;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import eu.gaiax.wizard.dao.repository.resource.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository repository;
    private final ParticipantRepository participantRepository;
    public ServiceOffer createResource(ResourceRequest request, String email) {
        Participant participant=participantRepository.getByEmail(email);

        Resource resource = Resource.builder()
                .name(request.resourceName())
                .type(request.type())
                .subType(request.subType()==null?null:request.subType())
                .description(request.description()==null?null:request.description())
                .participant(participant)
                .build();

        repository.save(resource);
        if(request.publish()){
            //publish to kafka
        }
        return null;
    }
}
