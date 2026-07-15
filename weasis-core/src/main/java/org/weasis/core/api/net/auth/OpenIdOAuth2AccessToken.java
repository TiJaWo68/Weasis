/*
 * Copyright (c) 2021 Weasis Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.weasis.core.api.net.auth;

import com.github.scribejava.core.model.OAuth2AccessToken;
import java.util.Objects;

/** OAuth2 access token with OpenID Connect id_token support. */
public class OpenIdOAuth2AccessToken extends OAuth2AccessToken {

  private final String openIdToken;

  public OpenIdOAuth2AccessToken(String accessToken, String openIdToken, String rawResponse) {
    this(accessToken, null, null, null, null, openIdToken, rawResponse);
  }

  public OpenIdOAuth2AccessToken(
      String accessToken,
      String tokenType,
      Integer expiresIn,
      String refreshToken,
      String scope,
      String openIdToken,
      String rawResponse) {
    super(accessToken, tokenType, expiresIn, refreshToken, scope, rawResponse);
    this.openIdToken = openIdToken;
  }

  public String getOpenIdToken() {
    return openIdToken;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), openIdToken);
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj
        || (obj instanceof OpenIdOAuth2AccessToken other
            && super.equals(obj)
            && Objects.equals(openIdToken, other.openIdToken));
  }
}
