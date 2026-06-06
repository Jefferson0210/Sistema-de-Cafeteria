export interface User {
  id:number; username:string; email:string; nombre:string; apellido:string;
  telefono?:string; roles:string[]; token:string; activo?:boolean; fechaRegistro?:string;fotoUrl?: string;
}
export interface Category { categoryId:number; name:string; descripcion:string; activo:boolean; }
export interface Producto {
  id:number; nombre:string; descripcion:string; precio:number; stock:number;
  disponible:boolean; imagenUrl:string; category?:Category; categoryId?:number;
}
export interface Mesa { id:number; numeroMesa:number; capacidad:number; ubicacion:string; estado:string; activo:boolean; }
export interface DetallePedido { id:number; producto:Producto; cantidad:number; precioUnitario:number; subtotal:number; notas?:string; }
export interface Pedido {
  id:number; mesa?:Mesa; cliente?:User; mesero?:User; estado:string;
  subtotal:number; total:number; notas?:string; detalles?:DetallePedido[]; fechaCreacion?:string;
}
export interface Factura {
  id:number; numeroFactura:string; subtotal:number; iva:number; descuento:number;
  total:number; estado:string; cliente?:User; cajero?:User; pedido?:Pedido; detalles?:any[];
}
export interface Reserva { id:number; mesa?:Mesa; usuario?:User; fecha:string; hora:string; personas:number; estado:string; notas?:string; }
export interface CartItem { producto:Producto; cantidad:number; notas:string; }
export interface ApiRes<T=any> { success:boolean; message:string; data:T; }
export type Rol = 'ADMIN'|'MESERO'|'CAJERO'|'CLIENTE';
