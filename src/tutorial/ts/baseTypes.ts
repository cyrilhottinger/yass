// shows how to use new base types

/// <reference path="../../main/ts/yass"/>

module contract {

    class Expiration_HANDLER implements yass.TypeHandler<Expiration> {
        read(reader: yass.Reader): Expiration {
            return new Expiration(
                yass.INTEGER_DESC.handler.read(reader),
                yass.INTEGER_DESC.handler.read(reader),
                yass.INTEGER_DESC.handler.read(reader)
            );
        }
        write(value: Expiration, writer: yass.Writer): void {
            yass.INTEGER_DESC.handler.write(value.year, writer);
            yass.INTEGER_DESC.handler.write(value.month, writer);
            yass.INTEGER_DESC.handler.write(value.day, writer);
        }
    }
    export class Expiration extends yass.Type {
        constructor(public year: number, public month: number, public day: number) {
            super();
        }
        static TYPE_DESC = new yass.TypeDesc(yass.FIRST_ID, new Expiration_HANDLER);
    }

    export module instrument.stock {
        class Double_HANDLER implements yass.TypeHandler<Double> {
            read(reader: yass.Reader): Double {
                return new Double(new DataView(reader.array.buffer).getFloat64(reader.needed(8)));
            }
            write(value: Double, writer: yass.Writer): void {
                var position = writer.needed(8);
                new DataView(writer.array.buffer).setFloat64(position, value.d);
            }
        }
        export class Double extends yass.Type {
            constructor(public d: number) {
                super();
            }
            static TYPE_DESC = new yass.TypeDesc(yass.FIRST_ID + 1, new Double_HANDLER);
        }
    }

}
