import moment from 'moment';

function fromNow(val: number) {
    return `${moment.utc(val).fromNow()}`;
}

function formatDate(val: number) {
    return moment.utc(val).format('YYYY-MM-DD HH:mm:ss[Z]');
}

export { fromNow, formatDate };
