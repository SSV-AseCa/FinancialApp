"""Per-user investor identity for load-test virtual users.

Two modes, selected by config.AUTH_MODE (env LOADTEST_AUTH_MODE):

* "jwt"    -- the API runs under the `loadtest` profile with an offline HS256
              decoder (LoadTestJwtDecoderConfig); we sign our own tokens here,
              no Auth0 round-trip.
* "header" -- the API runs under the `loadtest-nojwt` profile, which disables
              the resource server; we just send the subject in a plain header
              (LoadTestSubjectAuthenticationFilter reads it).

Either way each distinct subject becomes a distinct investor (lazy provisioning
keyed on `sub` in InvestorProvisioningFilter), so rotating the subject simulates
many investors.
"""

import itertools
import time

import jwt

from common import config

# Process-wide monotonic counter so every spawned user gets a unique subject.
_subject_counter = itertools.count(1)


def next_subject():
	"""Return a fresh, unique subject id for one virtual user."""
	return "loadtest-user-{0}".format(next(_subject_counter))


def mint_token(subject):
	"""Sign an HS256 JWT the API will accept under the loadtest profile."""
	now = int(time.time())
	claims = {
		"sub": subject,
		"iss": config.JWT_ISSUER,
		"aud": config.JWT_AUDIENCE,
		"iat": now,
		"nbf": now,
		"exp": now + config.JWT_TTL_SECONDS,
	}
	return jwt.encode(claims, config.JWT_SECRET, algorithm=config.JWT_ALGORITHM)


def auth_header_for_new_user():
	"""Return the auth header(s) for a brand-new investor subject.

	Picks the Bearer-token or plain-header form based on config.AUTH_MODE so the
	user classes stay identical across the `loadtest` and `loadtest-nojwt`
	profiles.
	"""
	subject = next_subject()
	if config.AUTH_MODE == "header":
		return {config.SUBJECT_HEADER: subject}
	return {"Authorization": "Bearer {0}".format(mint_token(subject))}
