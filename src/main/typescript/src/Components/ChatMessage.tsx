import * as React from "react";
import {Typography} from "@material-ui/core";
import "../styles.css";

const ChatMessage = ({text, username}) => {
    return(
        <div className="messageDiv">
            <Typography variant="display1" gutterBottom>
                {username}
            </Typography>
            <Typography variant="body1" gutterBottom>
                {text}
            </Typography>
        </div>
    )
}

export default ChatMessage;