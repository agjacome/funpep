import axios   from 'axios';
import api_url from '../config/api_url';

function status(uuid) {
  return axios.get(`${api_url}/analysis/${uuid}`);
}

function file(uuid, file) {
  return axios.get(`${api_url}/analysis/${uuid}/${file}`);
}

function image(uuid, file) {
  return axios
    .get(`${api_url}/analysis/${uuid}/${file}`, {
      responseType: 'arraybuffer'
    })
    .then(response => new Buffer(response.data, 'binary').toString('base64'))
}

function queueSize() {
  return axios.get(`${api_url}/analysis/queue`);
}

function queuePosition(uuid) {
  return axios.get(`${api_url}/analysis/queue/${uuid}`);
}

function sendAnalysis(json)
{
	return axios.post(`${api_url}/analysis/`, json);
}

export { status, image, file, queueSize, queuePosition, sendAnalysis };
