import axios   from 'axios';
import api_url from '../config/api_url';

function status(uuid) {
  return axios.get(`${api_url}/analysis/${uuid}`);
}

function file(uuid, file) {
  return axios.get(`${api_url}/analysis/${uuid}/${file}`);
}

function queueSize() {
  return axios.get(`${api_url}/analysis/queue`);
}

function queuePosition(uuid) {
  return axios.get(`${api_url}/analysis/queue/${uuid}`);
}

export { status, file, queueSize, queuePosition };
