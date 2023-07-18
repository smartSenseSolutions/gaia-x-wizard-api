package eu.gaiax.wizard.api.model;

import java.util.List;

public record RequiredActionsTokenRequest(
  String userId,
  String email,
  List<String> rqac,
  String redirectUri,
  Integer lifespan
) {
}
