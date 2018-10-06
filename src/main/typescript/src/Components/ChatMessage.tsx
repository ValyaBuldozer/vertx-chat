import * as React from "react";
import {Typography} from "@material-ui/core";
import "../styles.css";
import {observer} from "mobx-react";

const ChatMessage = ({message, username}) => {
    return(
        <div className="messageDiv">
            <Typography variant="display1" gutterBottom>
                {username}
            </Typography>
            <Typography variant="body1" gutterBottom>
                {message}
            </Typography>
        </div>
    )
}

export default observer(ChatMessage);