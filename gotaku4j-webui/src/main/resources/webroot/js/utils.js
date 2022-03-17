const KEY_GAME_ID = "game_id";
const KEY_USER_ID = "user_id";
const KEY_USER_NAME = "user_name";


function getUserId() {
  return getItem(KEY_USER_ID);
}

function setUserId(uid) {
  setItem(KEY_USER_ID, uid);
}

function getUserName() {
  return getItem(KEY_USER_NAME);
}

function setUserName(uname) {
  setItem(KEY_USER_NAME, uname);
}

function getGameId() {
  return getItem(KEY_GAME_ID);
}

function setGameId(gid) {
  setItem(KEY_GAME_ID, gid);
}

function addTempItems(itemsName, items) {
  setTempItems(itemsName, getTempItems(itemsName).concat(items))
}

function setTempItem(key, val) {
  window.sessionStorage.setItem(key, JSON.stringify(val));
}

function getTempItem(key) {
  const val = window.sessionStorage.getItem(key);
  if (val == null) { return null; }
  if (val === "undefined") { return null; }
  return JSON.parse(val);
}

function getTempItems(itemsName) {
  const items = getTempItem(itemsName);
  return items ? JSON.parse(items) : [];
}

function setTempItems(itemsName, items) {
  setTempItem(itemsName, JSON.stringify(items ? items : []));
}

function setItem(key, val) {
  window.localStorage.setItem(key, JSON.stringify(val));
}

function getItem(key) {
  const val = window.localStorage.getItem(key);
  if (val == null) { return null; }
  if (val === "undefined") { return null; }
  return JSON.parse(val);
}

function getItems(itemsName) {
  const items = getItem(itemsName);
  return items ? JSON.parse(items) : [];
}

function setItems(itemsName, items) {
  setItem(itemsName, JSON.stringify(items ? items : []));
}

function addItems(itemsName, items) {
  setItems(itemsName, getItems(itemsName).concat(items))
}

// localStorageに保存されている，あるkeyの値を削除する
function removeItem(key) {
  window.localStorage.removeItem(key);
}
// localStorageに保存されているすべての値を削除する
function clearLocalStorage() {
  window.localStorage.clear();
}

function isEnableLocalStorage() {
  try {
    window.localStorage.setItem("enableLocalStorage", "true");
    window.localStorage.getItem("enableLocalStorage");
    return true;
  } catch (e) {
    return false;
  }
}


function swalConfirm(title, text, type, callback) {
  Swal.fire({
    title: title,
    html: text ? text : null,
    type: type ? type : null,
    showCancelButton: true
  }).then((e) => {
    if (e.dismiss) {
      return;
    }
    callback(e);
  });
}


const swalToast = Swal.mixin({
  toast: true,
  type: 'info',
  timer: 3000,
})


function swalAlert(title, text, type, callback, confirmButtonText, showConfirmButton, timer) {
  Swal.fire({
    title: title,
    html: text ? text : null,
    icon: type ? type : null,
    confirmButtonText: confirmButtonText ? confirmButtonText : "OK",
    showConfirmButton: showConfirmButton===false? false:true,
    timer: timer ? timer: null
  }).then((result) => {
    if (!callback) {
      return;
    }
    callback(result);
  })
}

function swalInput(title, text, inputValue, inputPlaceholder, callback) {
  Swal.fire({
    title: title,
    input: 'text',
    html: text ? text : null,
    inputPlaceholder: inputPlaceholder,
    inputValue: inputValue,
    inputAttributes: {
      autocapitalize: 'off'
    },
    showCancelButton: true,
  }).then((result) => {
    callback(result.value);
  })
}

function swalTextArea(title, text, inputValue, inputPlaceholder, callback) {
  Swal.fire({
    title: title,
    input: 'textarea',
    html: text ? text : null,
    inputPlaceholder: inputPlaceholder,
    inputValue: inputValue,
    inputAttributes: {
      autocapitalize: 'off'
    },
    showCancelButton: true,
  }).then((result) => {
    callback(result.value);
  })
}


function toFormattedDate(milliseconds) {
  const date = new Date(milliseconds);
  const str = [date.getFullYear(), padding(date.getMonth() + 1), padding(date.getDate())].join('-');
  return str;
}

function toFormattedTime(milliseconds) {
  const date = new Date(milliseconds);
  const str = [padding(date.getHours()), padding(date.getMinutes()), padding(date.getSeconds())]
    .join(':');
  return str;
}

function padding(str) {
  return ('0' + str).slice(-2);
}

function toFormattedDateAndTime(milliseconds) {
  const str = toFormattedDate(milliseconds);
  str += ' ';
  str += toFormattedTime(milliseconds);
  return str;
}

function getCurrentDate() {
  return new Date().toJSON().slice(0, 10);
}


function stringifyEvent(e) {
  const obj = {};
  for (let k in e) {
    obj[k] = e[k];
  }
  return JSON.stringify(obj, (k, v) => {
    if (v instanceof Node) return 'Node';
    if (v instanceof Window) return 'Window';
    return v;
  }, ' ');
}

function toInt(text) {
  const sTxt = new String(text);
  const rText = text.replace(/\s+/g, "").replace(/[－ ー]/g, "-").replace(
    /[Ａ-Ｚａ-ｚ０-９]/g, function (s) {
      return String.fromCharCode(s.charCodeAt(0) - 0xFEE0);
    });
  try {
    const iText = parseInt(rText);
    if (Number.isNaN(iText)) { return false; }
    return iText;
  } catch (e) {
    return false;
  }
}
