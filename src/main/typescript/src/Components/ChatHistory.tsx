import * as React from "react";
import ChatMessage from "./ChatMessage";
import {observer} from "mobx-react";


const ChatHistory = ({messages}) => {

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

export default observer(ChatHistory);