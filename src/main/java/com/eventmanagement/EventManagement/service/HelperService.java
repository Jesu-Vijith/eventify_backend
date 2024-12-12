package com.eventmanagement.EventManagement.service;

import java.util.Random;

public class HelperService {

    public String idGenerator(){
        String CHAR_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int ID_LENGTH = 5;
        Random random = new Random();
        StringBuilder uniqueId = new StringBuilder(ID_LENGTH);

        for (int i = 0; i < ID_LENGTH; i++) {
            int index = random.nextInt(CHAR_SET.length());
            uniqueId.append(CHAR_SET.charAt(index));
        }
        return uniqueId.toString();
    }
}
