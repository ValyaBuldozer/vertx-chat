
export const authorizeRequest = (username: string, password: string)  => {
    return sendRequest("POST", "/authorize", {
        username: username,
        password: password
    });
};

export const registrationRequest = (username: string, password: string) => {
    return sendRequest("POST", "/register", {
        username: username,
        password: password
    });
};

export const socketConfigRequest = () => {
    return sendRequest("GET", "/socketconfig");
};

const sendRequest = (method: string, url: string, body?: any) => {
    return new Promise((resolve, reject) => {
        const request = new XMLHttpRequest();
        request.open(method, url);
        request.setRequestHeader("content-type", "application/json");
        request.onload = () => {
            if (request.status === 200) {
                resolve(request.response);
            } else {
                reject({
                    status: request.status,
                    statusText: request.statusText
                })
            }
        };
        request.onerror = () => {
            reject({
                status: request.status,
                statusText: request.statusText
            })
        };
        request.send(body ? JSON.stringify(body) : undefined);
    })
};
