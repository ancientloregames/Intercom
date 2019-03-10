'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

exports.onNewUser = functions.auth.user().onCreate(user => {
  const usersCollection = admin.firestore().collection('users');
  return usersCollection.doc(user.phoneNumber).set({
    'phone': user.phoneNumber
  });
});

exports.onCreateDialog = functions.firestore
  .document('users/{userId}/dialogs/{dialogId}')
  .onCreate((dialog, context) => {
    const dialogId = context.params.dialogId;
    const userId = context.params.userId;
    const recipientId = dialog.get('recipientId');
    
    const usersCollection = admin.firestore().collection('users');
    usersCollection.doc(`${userId}/contacts/${recipientId}`).set({
      'chatId': dialogId
    }, { merge: true });
    return usersCollection.doc(`${recipientId}/contacts/${userId}`).set({
      'phone': userId,
      'chatId': dialogId
    }, { merge: true });
});

exports.onNewMessage = functions.firestore
  .document('chats/{chatId}/messages/{messageId}')
  .onCreate((message, context) => {
    return message.ref.set({
      'timestamp': Date.now()
    }, { merge: true })
});