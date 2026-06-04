#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Módulo de conexión a base de datos MySQL
"""

import os
import mysql.connector
from mysql.connector import Error
from dotenv import load_dotenv

# Cargar variables de entorno
load_dotenv()

class DatabaseConnection:
    """Clase para manejar la conexión a la base de datos MySQL"""
    
    def __init__(self):
        self.connection = None
        self.host = os.getenv('DB_HOST', '192.168.35.25')
        self.port = int(os.getenv('DB_PORT', 3306))
        self.user = os.getenv('DB_USER', 'mmarco')
        self.password = os.getenv('DB_PASSWORD', '@System345')
        self.database = os.getenv('DB_NAME', 'pract')
    
    def create_connection(self):
        """Crear conexión a la base de datos"""
        try:
            self.connection = mysql.connector.connect(
                host=self.host,
                port=self.port,
                user=self.user,
                password=self.password,
                database=self.database,
                connection_timeout=10
            )
            if self.connection.is_connected():
                print(f"Conexión exitosa a MySQL: {self.host}:{self.port}/{self.database}")
                return True
        except Error as e:
            print(f"Error al conectar a MySQL: {e}")
            return False
    
    def close_connection(self):
        """Cerrar conexión a la base de datos"""
        if self.connection and self.connection.is_connected():
            self.connection.close()
            print("Conexión a MySQL cerrada")
    
    def execute_query(self, query, params=None):
        """Ejecutar consulta y retornar resultados"""
        if not self.connection or not self.connection.is_connected():
            if not self.create_connection():
                return None
        
        try:
            cursor = self.connection.cursor(dictionary=True)
            cursor.execute(query, params)
            results = cursor.fetchall()
            cursor.close()
            return results
        except Error as e:
            print(f"Error al ejecutar consulta: {e}")
            return None
    
    def get_pedidos_summary(self):
        """Obtener el resumen de pedidos según la consulta especificada"""
        query = """
        SELECT 
            COALESCE(DATOS.USUARIO, 'TOTAL GENERAL') AS USUARIO,
            COALESCE(USU.NOMBRE, '') AS NOMBRE,
            COUNT(*) AS NUM_PEDIDOS,
            SUM(DATOS.IMPORTE) AS IMPORTE_TOTAL,
            AVG(DATOS.IMPORTE) AS IMPORTE_MEDIO_POR_PEDIDO
        FROM 
        (
            SELECT USUARIO, NUMPED,
                   COUNT(*) AS LINEAS, 
                   SUM(CANTIDAD * PRECIO) AS IMPORTE 
            FROM pract.PEDIDOS_WEB
            WHERE USUARIO NOT IN ('001', '002', '003')
            AND PRECIO > 0
            GROUP BY USUARIO, NUMPED
        ) AS DATOS
        INNER JOIN pract.USUARIOS_LOGIN USU ON USU.COD_USER = DATOS.USUARIO
        GROUP BY DATOS.USUARIO, USU.NOMBRE 
        ORDER BY USUARIO, IMPORTE_TOTAL DESC
        """
        return self.execute_query(query)
    
    def get_pedidos_by_usuario(self, usuario):
        """Obtener lista de pedidos para un usuario específico"""
        query = """
        SELECT 
            DATOS.USUARIO,
            COALESCE(USU.NOMBRE, '') AS NOMBRE,
            DATOS.NUMPED,
            DATOS.ESTADO,
            DATOS.LINEAS,
            DATOS.IMPORTE
        FROM 
        (
            SELECT USUARIO, NUMPED, ESTADO,
                   COUNT(*) AS LINEAS, 
                   SUM(CANTIDAD * PRECIO) AS IMPORTE 
            FROM pract.PEDIDOS_WEB
            WHERE USUARIO NOT IN ('001', '002', '003')
            AND PRECIO > 0
            GROUP BY USUARIO, NUMPED, ESTADO
        ) AS DATOS
        INNER JOIN pract.USUARIOS_LOGIN USU ON USU.COD_USER = DATOS.USUARIO
        WHERE DATOS.USUARIO = %s
        ORDER BY DATOS.NUMPED DESC
        """
        return self.execute_query(query, (usuario,))
    
    def get_pedido_detalle(self, usuario, numped):
        """Obtener detalle completo de un pedido específico"""
        query = """
        SELECT 
            PW.NUMPED,
            PW.USUARIO,
            COALESCE(USU.NOMBRE, '') AS NOMBRE,
            PW.ESTADO,
            PW.ARTI AS CODART,
            PW.ARTI AS DESART,
            PW.CANTIDAD,
            PW.PRECIO,
            PW.DESCUENTO1,
            PW.DESCUENTO2,
            (PW.CANTIDAD * PW.PRECIO) AS IMPORTE_LINEA,
            PW.FECHA_CREACION AS FECHA_PEDIDO
        FROM pract.PEDIDOS_WEB PW
        INNER JOIN pract.USUARIOS_LOGIN USU ON USU.COD_USER = PW.USUARIO
        WHERE PW.USUARIO = %s AND PW.NUMPED = %s
        ORDER BY PW.ID
        """
        return self.execute_query(query, (usuario, numped))
    
    def get_pedido_resumen(self, usuario, numped):
        """Obtener resumen de un pedido (encabezado)"""
        query = """
        SELECT 
            PW.NUMPED,
            PW.USUARIO,
            COALESCE(USU.NOMBRE, '') AS NOMBRE,
            PW.ESTADO,
            COUNT(*) AS TOTAL_LINEAS,
            SUM(PW.CANTIDAD * PW.PRECIO) AS IMPORTE_TOTAL,
            MIN(PW.FECHA_CREACION) AS FECHA_PEDIDO
        FROM pract.PEDIDOS_WEB PW
        INNER JOIN pract.USUARIOS_LOGIN USU ON USU.COD_USER = PW.USUARIO
        WHERE PW.USUARIO = %s AND PW.NUMPED = %s
        GROUP BY PW.NUMPED, PW.USUARIO, USU.NOMBRE, PW.ESTADO
        """
        return self.execute_query(query, (usuario, numped))

# Instancia global para reutilizar conexión
db = DatabaseConnection()
