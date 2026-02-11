package tn.esprim.knowledgehub.resource.enums;

public enum SubmissionStatus {
    DRAFT,              // Student is preparing
    SUBMITTED,          // Submitted to supervisor
    UNDER_REVIEW,       // Supervisor is reviewing
    REJECTED,           // Supervisor rejected - needs revision
    APPROVED,           // Supervisor approved
    SELECTED_FOR_LIBRARY,  // Admin selected for publication
    PUBLISHED           // Available in library
}
