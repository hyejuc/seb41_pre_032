// 추후 링크 수정 필요
const BASE_URL =
  'http://pre-project-32-front.s3-website.ap-northeast-2.amazonaws.com/';
const QUESTION_URL =
  'http://pre-project-32-front.s3-website.ap-northeast-2.amazonaws.com/question/';

export const fetchCreate = (url, data, authorization, refresh) => {
  fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: authorization,
      Refresh: refresh,
    },
    body: JSON.stringify(data),
  })
    .then(() => {
      window.location.href = BASE_URL;
    })
    .catch((error) => {
      console.error('Error', error);
    });
};

export const fetchDelete = (url, id, authorization, refresh) => {
  fetch(`${url}${id}`, {
    method: 'DELETE',
    headers: {
      Authorization: authorization,
      Refresh: refresh,
    },
  })
    .then(() => {
      window.location.href = BASE_URL;
    })
    .catch((error) => {
      console.error('Error', error);
    });
};

export const fetchPatch = (url, id, data, authorization, refresh) => {
  fetch(`${url}${id}`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'Application/json',
      Authorization: authorization,
      Refresh: refresh,
    },
    body: JSON.stringify(data),
  })
    .then(() => {
      window.location.href = `${QUESTION_URL}${id}`;
    })
    .catch((error) => {
      console.error('Error', error);
    });
};
