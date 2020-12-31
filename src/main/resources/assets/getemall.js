/*!
 * getemall.js
 * (c) 2020 Miquifant
 */
const getResponseOrBreak = (jsonData) => {
  // the attribute 'id' is expected for every returned entity,
  // except when returned object is an error (code, message)
  if (jsonData.id) return jsonData;
  else throw Error(jsonData.message || jsonData.title || "unknown error");
};
const getResponseArrayOrBreak = (jsonData) => {
  // an array is expected unless there is an error
  if (Array.isArray(jsonData)) return jsonData;
  else throw Error(jsonData.message || jsonData.title || "unknown error");
};
const handleFetchList = (endpoint, handler, errHandler) => {
  return fetch("/api/" + endpoint, {
      method: "get"
  }).then(res => res.json())
    .then(getResponseArrayOrBreak)
    .then(handler)
    .catch((e) => {
        console.log("Error fetching " + endpoint + ": " + e);
        if (errHandler) errHandler(e);
        else alert(e.message);
    })
};
const handleFetch = (endpoint, query, handler, errHandler) => {
  return fetch("/api/" + endpoint + "/" + query, {
      method: "get"
  }).then(res => {
        // when res.status is 404 server doesn't send any json, so we create it
        if (res.status == 404)
          return JSON.parse('{"code":"'+res.status+'","message":"Not found"}')
        else return res.json()
    })
    .then(getResponseOrBreak)
    .then(handler)
    .catch((e) => {
        console.log("Error fetching " + endpoint + ": " + e);
        if (errHandler) errHandler(e);
        else alert(e.message);
    })
};
const handleSave = (endpoint, obj, handler, errHandler) => {
  return fetch("/api/" + endpoint, {
      method: (obj.id && obj.id !== 0)? "put": "post",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(obj)
  }).then(res => res.json())
    .then(getResponseOrBreak)
    .then(handler)
    .catch((e) => {
        console.log("Error saving " + endpoint + ": " + e);
        if (errHandler) errHandler(e);
        else alert(e.message);
    })
};
const handleDelete = (endpoint, id, handler, errHandler) => {
  return fetch("/api/" + endpoint + "/" + id, {
      method: "delete"
  }).then(res => res.json())
    .then(getResponseOrBreak)
    .then(handler)
    .catch((e) => {
        console.log("Error deleting " + endpoint + ": " + e);
        if (errHandler) errHandler(e);
        else alert(e.message);
    })
};
