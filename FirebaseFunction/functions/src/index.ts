import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import { DocumentSnapshot } from 'firebase-functions/lib/providers/firestore';

// // Start writing Firebase Functions
// // https://firebase.google.com/docs/functions/typescript
//
// export const helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

admin.initializeApp(functions.config().firebase);

export const sendNotification = functions.firestore
    .document('users/{userId}/notifications/{notificationId}')
    .onCreate(function (snapshot: DocumentSnapshot, context: functions.EventContext) {
        const receiveId = context.params.userId;
        const { send_id, message } = snapshot.data();
        console.log(`SendId: ${send_id}, Message: ${message}, ReceiveId: ${receiveId}`);

        const getSenderPromise: Promise<DocumentSnapshot> = admin.firestore().doc(`users/${send_id}`).get();
        const getReceiverPromise: Promise<DocumentSnapshot> = admin.firestore().doc(`users/${receiveId}`).get();

        return Promise.all([getSenderPromise, getReceiverPromise]).then(function (value: DocumentSnapshot[]) {
            const sender = value[0].data();
            const receiver = value[1].data();

            const payload: admin.messaging.MessagingPayload = {
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
                .then(function (response: admin.messaging.MessagingDevicesResponse) {
                    response.results
                        .forEach(function (result: admin.messaging.MessagingDeviceResult) {
                            if (result.error) {
                                console.log('Failure sending notification: ', result.error);
                            } else {
                                console.log('Send sucessfully, messageId: ', result.messageId);
                            }
                        });
                });
        });
    });