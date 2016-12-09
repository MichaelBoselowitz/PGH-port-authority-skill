package com.maya.portAuthority.storage;

import com.amazon.speech.speechlet.Session;

/**
 * Contains the methods to interact with the persistence layer for Port Authority in DynamoDB.
 */
public class PaDao {
    private final PaDynamoDbClient dynamoDbClient;

    public PaDao(PaDynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * Reads and returns the user information from the session.
     * <p>
     * Returns null if the item could not be found in the database.
     * 
     * @param session
     * @return
     */
    public PaInput getPaInput(Session session) {
        PaUserDataItem item = new PaUserDataItem();
        item.setCustomerId(session.getUser().getUserId());

        item = dynamoDbClient.loadItem(item);

        if (item == null) {
            return null;
        }

        return PaInput.newInstance(session, item.getInputData());
    }

    /**
     * Saves the {@link PaInput} into the database.
     * 
     * @param input
     */
    public void saveScoreKeeperGame(PaInput input) {
        PaUserDataItem item = new PaUserDataItem();
        item.setCustomerId(input.getSession().getUser().getUserId());
        //item.setGameData(game.getGameData());
        item.setData(input.getData());
        dynamoDbClient.saveItem(item);
    }
}
