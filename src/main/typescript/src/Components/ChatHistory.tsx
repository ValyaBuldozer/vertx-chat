import * as React from "react";
import {ChatMessage} from "./ChatMessage";



export const ChatHistory = ({messages}) => {

    return(
        <div>
            {messages.map((msg) => {
                return(
                    <ChatMessage message={msg.message} username={msg.username}/>
                )
            })}
        </div>
    )
}