"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const functions = require("firebase-functions");
const admin = require("firebase-admin");
// // Start writing Firebase Functions
// // https://firebase.google.com/docs/functions/typescript
//
// export const helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });
admin.initializeApp(functions.config().firebase);
exports.sendNotification = functions.firestore
    .document('users/{userId}/notifications/{notificationId}')
    .onCreate(function (snapshot, context) {
    const receiveId = context.params.userId;
    const { send_id, message } = snapshot.data();
    console.log(`SendId: ${send_id}, Message: ${message}, ReceiveId: ${receiveId}`);
    const getSenderPromise = admin.firestore().doc(`users/${send_id}`).get();
    const getReceiverPromise = admin.firestore().doc(`users/${receiveId}`).get();
    return Promise.all([getSenderPromise, getReceiverPromise]).then(function (value) {
        const sender = value[0].data();
        const receiver = value[1].data();
        const payload = {
            data: {
                sender_name: sender.name.toString(),
                sender_id: send_id.toString(),
                sender_image_url: sender.image_url.toString()
            },
            notification: {
                title: `${receiver.name},  you haven received from ${sender.name}`,
                body: message.toString(),
                clickAction: 'com.hoc.firebasepushnotification.NotificationActivity'
            }
        };
        const token = receiver.token;
        console.log(`payload: ${payload}, token: ${token}`);
        return admin.messaging()
            .sendToDevice(token, payload)
            .then(function (response) {
            response.results
                .forEach(function (result) {
                if (result.error) {
                    console.log('Failure sending notification: ', result.error);
                }
                else {
                    console.log('Send sucessfully, messageId: ', result.messageId);
                }
            });
        });
    });
});
//# sourceMappingURL=index.js.map