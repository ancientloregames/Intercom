'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

const chats = admin.firestore().collection('chats');
const users = admin.firestore().collection('users');

exports.onNewUser = functions.auth.user().onCreate(user => {
  const usersCollection = admin.firestore().collection('users');
  return usersCollection.doc(user.phoneNumber).set({
    'phone': user.phoneNumber
  });
});

exports.onCreateChat = functions.firestore
  .document('chats/{chatId}')
  .onCreate((chat, context) => {
    const chatId = context.params.chatId;
    const chatName = chat.get('name');

    // dialog chat
    if (chatName === '') {
      const initiatorId = chat.get('initiatorId');
      const recipientId = chat.get('participants').find(userId => userId != initiatorId);
      users.doc(recipientId).collection('contacts').doc(initiatorId).get(snap => {
        const contactName = snap.get('name');
        users.doc(recipientId).collection('chats').doc(chatId).set({
          'id': chatId,
          'name': contactName
        }, { merge: true });
        snap.ref.set({
          'chatId': chatId
        }, { merge: true });
      });
      return users.doc(initiatorId).collection('contacts').doc(recipientId).get(snap => {
        const contactName = snap.get('name');
        users.doc(initiatorId).collection('chats').doc(chatId).set({
          'id': chatId,
          'name': contactName
        }, { merge: true });
        snap.ref.set({
          'chatId': chatId
        }, { merge: true });
      });
    } 
    // group chat
    else {
      return chat.get('participants').forEach(userId => {
          users.doc(userId).collection('chats').doc(chatId).set({
            'id': chatId,
            'name': chatName
          }, { merge: true });
      });
    }
});

exports.onCreateMessage = functions.firestore
  .document('chats/{chatId}/messages/{messageId}')
  .onCreate((message, context) => {
    const timestamp = message.get('timestamp');
    const text = message.get('text');

    const chatId = context.params.chatId;
    return chats.doc(chatId).get().then(snap => {
      snap.get('participants').forEach(userId => {
        users.doc(userId).collection('chats').doc(chatId).set({
          'lastMsgTime': timestamp,
          'lastMsgText': text
        }, { merge: true });
      });
    });
});