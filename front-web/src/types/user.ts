import { Role } from "./role";

export type User = {
    id: number,
    firstName: string,
    lastName: string,
    email: string,
    password: string,
    roles: Role[];
}

