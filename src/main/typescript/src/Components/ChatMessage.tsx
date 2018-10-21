import * as React from "react";
import {Typography} from "@material-ui/core";
import "../styles.css";

const ChatMessage = ({text, username, timestamp}) => {
    return(
        <div className="msgRoot">
            <div className="msgHeader">
                <p>
                    {username}
                </p>
                <span className="msgTimestamp">
                    <p>
                        {timestamp}
                    </p>
                </span>
            </div>
            <div className="msgBody">
            <Typography variant="body1" gutterBottom>
                {text}
            </Typography>
            </div>
        </div>
    )
}

export default ChatMessage;