public boolean hasValidSearchCriteria() {
    return StringUtils.hasText(firstName) || 
           StringUtils.hasText(lastName) || 
           dateOfBirth != null;
}
