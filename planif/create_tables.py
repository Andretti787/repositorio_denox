# /project_root/create_tables.py
from database import engine, Base
from models import MaquinaInyectora, Articulo, MaquinaArticuloProduccion, Orden, PlanificacionResultado # Importa todos tus modelos actuales

def main():
    print("Creando tablas en la base de datos...")
    Base.metadata.create_all(bind=engine)
    print("Tablas creadas exitosamente.")

if __name__ == "__main__":
    main()
