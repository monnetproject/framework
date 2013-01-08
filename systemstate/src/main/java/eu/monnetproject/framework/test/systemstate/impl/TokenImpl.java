package eu.monnetproject.framework.test.systemstate.impl;

import eu.monnetproject.framework.test.systemstate.SystemStateService.Token;

public final class TokenImpl implements Token {
	private final Object reference;

	public TokenImpl(Object reference) {
		this.reference = reference;
	}
	
	@Override
	public final String toString() {
		return "Token[" + reference + "]";
	}
}
