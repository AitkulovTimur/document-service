package com.ITQ.document_service.dto.request;

import java.util.Collection;

/**
 * Generic interface for batch request DTOs in the document service.
 * 
 * <p>This interface defines a contract for batch operations that need to process
 * multiple items with associated comments and track the actor performing the operation.
 * It is designed to work with any type that extends {@link HasId}, ensuring that
 * each item in the batch has a unique identifier.</p>
 * <p><strong>Implementation Notes:</strong></p>
 * <ul>
 *   <li>Implementations should validate that the collection is not empty</li>
 *   <li>Consider limiting the maximum batch size for performance reasons</li>
 *   <li>The actor field should represent the user or system performing the operation</li>
 * </ul>
 * 
 * @param <T> the type of items in the batch, must extend {@link HasId}
 * @see HasId
 * @see BatchApprovalRequest
 * @see BatchSubmissionRequest
 */
public interface BatchRequest<T extends HasId> {
    /**
     * Returns the collection of items to be processed in this batch.
     * 
     * <p>Each item in the collection should contain an identifier and any associated
     * comments or metadata required for processing the operation.</p>
     * 
     * @return a non-null collection of items extending {@link HasId}
     */
    Collection<T> idsWithComments();
    
    /**
     * Returns the identifier of the actor performing this batch operation.
     * 
     * <p>The actor typically represents a username, user ID, or system identifier
     * responsible for initiating the batch operation. This information is used
     * for auditing and logging purposes.</p>
     * 
     * @return a non-null, non-blank string identifying the actor
     */
    String actor();
}