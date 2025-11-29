@Override
public CredentialValidationResult validate(UsernamePasswordCredential cred) {
    SecurityUser user = jpaHelper.findUserByName(cred.getCaller());
    if (user == null) {
        return CredentialValidationResult.INVALID_RESULT;
    }
    if (!PasswordUtil.verifyPassword(cred.getPasswordAsString(), user.getPwHash())) {
        return CredentialValidationResult.INVALID_RESULT;
    }
    Set<String> roles = jpaHelper.getRoles(user);
    return new CredentialValidationResult(user, roles);
}
