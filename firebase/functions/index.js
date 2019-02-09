'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

exports.onNewUser = functions.auth.user().onCreate((user) => {
  return admin.firestore().ref('users/${user.uid}').set({
    phoneNumber: user.phoneNumber
  });
});

exports.onAddContact = functions.firestore
  .document('users/{userId}/contacts/{contactId}')
  .onCreate((contact, context) => {
    const contactRef = contact.ref;
    const contactPhone = contact.get('phone');
    
    const usersCollection = admin.firestore().collection('users');
    return usersCollection.where('phone', '==', contactPhone).limit(1).get()
      .then(users => {
        users.forEach(user => {
          if (user.exists)
            contactRef.set({ uid: user.id }, { merge: true });
        });
      });
});
