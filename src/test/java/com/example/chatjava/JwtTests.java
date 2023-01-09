package com.example.chatjava;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JwtTests {
    @Test
    void canGetAndValidateAJwtToken() throws Exception {
        String jwkStr = """
            {"keys":[{"kty":"RSA","e":"AQAB","use":"sig","kid":"Default-key","alg":"RS256","n":"vbi9UqKqak0F3miVdJwcl3lQ0RjbPp6b4--PktuJvzTAKFmN0mt_u2VuJZKqI6uCU6dcLEkknOFlxUeqSeHy74Rq_WzAIJgu_U61qWK0gsYM908ye2adcPKyS7yk0NSArrhZ4Kc7B-bprt8thc5Y4wht4YPf1LXdIKTCKsTj-yG5q-lp8_bH8iiD3m8KyQw0sTv4sN0kW7oZ4rpHomaRL6qRB6yJkecLmfRMXmuOenNJ-CrzEMVmI_irjD114PYFfVtL234FBXEMcaNL-mSplNDYePfa-Q5v2Mh1Hu9qq_Ltmf5FFUzfn7Q7z0-vuOcDbZ9dNM_JB739AKjCk8FzXQ"}]}
            """;
        SimpleJwtDecoder jwtDecoder = new SimpleJwtDecoder(new StringJwkSource(jwkStr));

        Assertions.assertDoesNotThrow(() -> jwtDecoder.decode(TestConstants.Token));
    }
}
