export class Error {
    header = 'Oops! Something went wrong!';
    httpStatus: number;
    message: string;


    constructor(httpStatus: number, message: string) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
