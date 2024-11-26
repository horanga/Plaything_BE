package com.plaything.api.domain.repository.entity.chat;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String senderNickname;

    @Column
    private String receiverNickname;

    @Column
    private String exitedUserNickname;

    @Column
    private boolean isClosed = false;


    public boolean validateRequester(String name){
        return senderNickname.equals(name) || receiverNickname.equals(name);
    }

    public void leaveChatRoom(String nickName){
        if(this.exitedUserNickname.isBlank()){
            this.exitedUserNickname = nickName;
        } else {
            isClosed = true;
        }
    }

}
