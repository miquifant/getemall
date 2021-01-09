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
  }).then(getResponseOrBreak)
    .then(handler)
    .catch((e) => {
        console.log("Error fetching " + endpoint + ": " + e);
        if (errHandler) errHandler(e);
        else alert(e.message);
    })
};
const handleCheck = (endpoint, query, handler, errHandler) => {
  return fetch("/api/" + endpoint + "/" + query, {
      method: "head"
  }).then(res => {
      if (res.status == 404) return false;
      else if (res.status == 200) return true;
      else throw Error(res.status);
    },
    rej => {
      throw Error("Rejected")
    })
    .then(handler)
    .catch((e) => {
        console.log("Error checking " + endpoint + ": " + e);
        if (errHandler) errHandler(e);
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
// patch resource according to RFC-7386
const handlePatch = (endpoint, query, patch, handler, errHandler) => {
  return fetch("/api/" + endpoint + "/" + query, {
      method: "PATCH",
      headers: { "Content-Type": "application/merge-patch+json" },
      body: JSON.stringify(patch)
  }).then(res => {
      if (res.status == 204) return null;
      else return res.json()
  }).then(errJson => {
      if (errJson) throw Error(errJson.message || errJson.title || "unknown error")
  }).then(handler)
    .catch((e) => {
        console.log("Error patching " + endpoint + ": " + e);
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
