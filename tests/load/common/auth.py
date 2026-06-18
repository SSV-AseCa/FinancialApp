"""Local JWT minting for load-test virtual users.

The API runs under the `loadtest` profile with an offline HS256 decoder
(LoadTestJwtDecoderConfig), so we sign our own tokens here -- no Auth0 round-trip.
Each distinct `sub` becomes a distinct investor (lazy provisioning keyed on `sub`
in InvestorProvisioningFilter), so rotating the subject simulates many investors.
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
	"""Convenience: mint a token for a brand-new subject and return the header."""
	token = mint_token(next_subject())
	return {"Authorization": "Bearer {0}".format(token)}
