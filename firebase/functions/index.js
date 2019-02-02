'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

exports.onUser = functions.auth.user().onCreate((user) => {
  return db.ref('users/${user.uid}').set({
    phoneNumber: user.phoneNumber
  })
});
