package com.inspectuspro.api.common.id;

import com.github.f4b6a3.uuid.UuidCreator;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class IdGenerator {
	public UUID newId() {
		return UuidCreator.getTimeOrderedEpoch();
	}
}

